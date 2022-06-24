package com.dolabs.emircom.connector;


import static com.dolabs.emircom.utils.Constants.SEC_HEAD;
import static com.dolabs.emircom.utils.Constants.SEC_KEY;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.dolabs.emircom.R;
import com.dolabs.emircom.session.SessionContext;
import com.dolabs.emircom.utils.Commons;
import com.dolabs.emircom.utils.MimeUtils;
import com.dolabs.emircom.utils.file_handling.FileUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import kotlin.text.Charsets;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by Jose on 3/16/16.
 */
public class HTTPConnector  {

    private final String TAG = this.getClass().getSimpleName();
    private final Context mContext;
    private final ConnectorTransfer connectorTransfer;
    private final ServiceResponse serviceResponse;
    private int responseCode = HTTP_OK;

    private final boolean disableDebug;
    private final SessionContext sessionContext;
    private ExecuteServiceCall executeServiceCall;

    public static long PROGRESS_UPLOAD = 0;
    public static long PROGRESS_DOWNLOAD = 1;

    public HTTPConnector(Context context, ConnectorTransfer connectorTransfer, ServiceResponse serviceResponse) {

        this.mContext = context;
        this.connectorTransfer = connectorTransfer;
        this.serviceResponse = serviceResponse;
        disableDebug = connectorTransfer.isDisableDebugging();
        sessionContext = SessionContext.getInstance();
    }

    public void execute() {

        try {

            if(executeServiceCall != null) {

                if(executeServiceCall.getStatus() == AsyncTask.Status.PENDING || executeServiceCall.getStatus() == AsyncTask.Status.RUNNING) {

                    cancel(true);
                    executeServiceCall = null;
                }
            }

            executeServiceCall = new ExecuteServiceCall();
            executeServiceCall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        cancel(true);
    }

    public void cancel(boolean mayInterruptIfRunning) {

        try {

            if(executeServiceCall != null) {
                executeServiceCall.cancel(mayInterruptIfRunning);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ExecuteServiceCall extends AsyncTask<String, Long, String> {

        String functionName = "";

        Exception processException = null;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {

            String responseString = null;

            try {

                functionName = connectorTransfer.getFunctionName();

                if(connectorTransfer.isSimulateResponseError()) {
                    processException = new Exception("Simulated Error Response");
                    return null;
                }

                String simulatedResponseFile = connectorTransfer.getSimulatedResponseFile();

                if(simulatedResponseFile == null) {

                    Request.Builder requestBuilder = new Request.Builder();

                    //Construct URL query params
                    StringBuilder constructedUrl = null;

                    if(connectorTransfer.getRequestUrl() != null)
                        constructedUrl = new StringBuilder(connectorTransfer.getRequestUrl().trim());

                    if(constructedUrl == null) {

                        processException = new Exception("Request URL should not be null!");
                        return null;
                    }

                    if(functionName == null) {

                        String[] urlParamsArr = constructedUrl.toString().split("/");
                        if(urlParamsArr.length > 0) {

                            int pos = urlParamsArr.length - 1;
                            functionName = "" + urlParamsArr[pos];
                        }
                    }

                    constructedUrl = new StringBuilder(constructedUrl.toString().trim());

                    boolean isFirstItem = true;
                    LinkedHashMap<String, String> reqQueryParamsMap = connectorTransfer.getQueryParams();
                    if(reqQueryParamsMap != null) {

                        if(constructedUrl.toString().contains("?")) {

                            if(constructedUrl.toString().endsWith("?")) {
                                constructedUrl = new StringBuilder(constructedUrl.substring(0, constructedUrl.length() - 1));
                                isFirstItem = true;
                            }

                            isFirstItem = false;
                        }

                        for (LinkedHashMap.Entry<String, String> entry : reqQueryParamsMap.entrySet()) {

                            String key = entry.getKey();
                            String value = entry.getValue();

                            if(isFirstItem) {

                                constructedUrl.append("?").append(key).append("=").append(value);
                                isFirstItem = false;
                            }
                            else {

                                constructedUrl.append("&").append(key).append("=").append(value);
                            }

                            if(!disableDebug)
                                Log.d(TAG + " Query Param " + functionName, key + "=" + value);
                        }
                    }

                    //Construct URL path params
                    LinkedHashMap<String, String> reqPathParamsMap = connectorTransfer.getPathParams();
                    if(reqPathParamsMap != null) {

                        for (LinkedHashMap.Entry<String, String> entry : reqPathParamsMap.entrySet()) {

                            String key = entry.getKey();
                            String value = entry.getValue();

                            String pathParam = (key + "/" + value);
                            String pathSeparator = "";

                            if(!constructedUrl.toString().endsWith("/")) {
                                pathSeparator = "/";
                            }

                            constructedUrl.append(pathSeparator).append(pathParam);

                            if(!disableDebug)
                                Log.d(TAG + " Path Param " + functionName, pathParam);
                        }
                    }

                    if(!disableDebug)
                        Log.d(TAG + " Request URL " + functionName, constructedUrl.toString());

                    URL url = new URL(constructedUrl.toString());
                    requestBuilder.url(url);

                    boolean isArabic = sessionContext.isArabic();
                    String langParam = isArabic ? "ar" : "en";

                    //Construct Request Headers
                    LinkedHashMap<String, String> reqHeaderMap = connectorTransfer.getRequestHeaders();
                    if(reqHeaderMap == null) {
                        reqHeaderMap = new LinkedHashMap<>();
                    }

                    //set default headers
                    reqHeaderMap.put(ContentType.getHeaderKey(), connectorTransfer.getContentType().headerName());
                    reqHeaderMap.put("Cache-Control", "no-cache");
                    reqHeaderMap.put("Pragma", "no-cache");
                    reqHeaderMap.put("Accept", "application/json; odata=verbose");
                    reqHeaderMap.put("Lang", langParam);
                    reqHeaderMap.put("Malabhootham", SEC_HEAD);

                    String authorization = connectorTransfer.getCustomAuthorization();

                    if(authorization == null || authorization.trim().isEmpty()) {

                        if(functionName.equalsIgnoreCase("register") || functionName.equalsIgnoreCase("verifyotp") || functionName.equalsIgnoreCase("token")) {

                            authorization = sessionContext.getBearerToken();
                        }
                        else {

                            authorization = "bearer " + sessionContext.getAccessToken();
                        }

                        String urlHost = url.getHost();

                        /*if(urlHost.endsWith(HOST_EHUB) || (!Config.IS_DUKKAN_PRODUCTION && urlHost.startsWith("dukkan"))) {

                            authorization = sessionContext.getDukkanAccessToken();
                        }
                        else if(urlHost.equalsIgnoreCase(MIRSAL_ASSETS_HOST)) {

                            authorization = SessionContext.getInstance().getAssetTokenType() + " " + SessionContext.getInstance().getAssetToken();
                        }
                        else {

                            authorization = sessionContext.getAccessToken();
                        }*/
                    }

                    if(authorization != null && !authorization.trim().isEmpty()) {
                        reqHeaderMap.put("Authorization", authorization);
                    }

                    for (LinkedHashMap.Entry<String, String> entry : reqHeaderMap.entrySet()) {

                        String key = entry.getKey();
                        String value = entry.getValue();

                        requestBuilder.header(key, value);

                        if(!disableDebug)
                            Log.d(TAG + " Header Param " + functionName, key + ": " + value);
                    }

                    //Construct RequestBody
                    if(connectorTransfer.getActionType() != HTTPActionType.GET) {

                        LinkedHashMap<String, Object> requestBodyMap = connectorTransfer.getRequestBodyMap();
                        if(requestBodyMap == null)
                            requestBodyMap = new LinkedHashMap<>();

                        requestBodyMap.put("Lang",  langParam);
                        requestBodyMap.put("secKey",  SEC_KEY);

                        String contentType = reqHeaderMap.get(ContentType.getHeaderKey());
                        MediaType httpActionType = MediaType.parse(connectorTransfer.getActionType().name() + "; charset=utf-8");
                        RequestBody requestBody;

                        if(contentType.equalsIgnoreCase(ContentType.MULTIPART_FORM_DATA.headerName())) {

                            MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                            List<ConnectorTransfer.FilePart> fileParts = connectorTransfer.getFileParts();

                            StringBuilder logValue = new StringBuilder("\n");

                            if(fileParts!=null)
                            {
                                for (ConnectorTransfer.FilePart filePart: fileParts) {

                                    Uri fileUri = Uri.parse(filePart.fileUri);
                                    String mimeType = MimeUtils.guessMimeTypeFromUri(fileUri);
                                    File file = new File(FileUtils.getPath(mContext, fileUri));

                                    logValue.append("File Key - ").append(filePart.fileKey)
                                            .append(", File -").append(file.getAbsolutePath()).append("\n");

                                    RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), file);
                                    multipartBody.addFormDataPart(filePart.fileKey, file.getName(), fileBody);
                                }
                            }


                            for (Map.Entry<String, Object> entry : requestBodyMap.entrySet()) {

                                String key = entry.getKey();
                                Object value = entry.getValue();

                                if(key == null || value == null)
                                    continue;

                                multipartBody.addFormDataPart(key, value.toString());

                                logValue.append(key).append(": ").append(value.toString()).append("\n");
                            }

                            if (!disableDebug)
                                Log.d(TAG + " Request Body " + functionName, logValue.toString());

                            requestBody = multipartBody.build();
                        }
                        else {

                            String requestBodyStr = null;

                            if(connectorTransfer.getCustomRequestString() != null) {

                                requestBodyStr = connectorTransfer.getCustomRequestString();
                            }
                            else if(contentType.equalsIgnoreCase(ContentType.APPLICATION_JSON.headerName())) {

                               /* JSONObject requestObject = new JSONObject();

                                for (Map.Entry<String, Object> entry : requestBodyMap.entrySet()) {

                                    String key = entry.getKey();
                                    Object value = entry.getValue();

                                    if(key == null || value == null)
                                        continue;

                                    requestObject.put(key, value);
                                }*/

                                if(requestBodyMap!=null)
                                {
                                    Gson gson = new Gson();
                                    String objJson = gson.toJson(requestBodyMap);
                                    requestBodyStr = objJson;
                                }

                            }
                            else if(contentType.equalsIgnoreCase(ContentType.APPLICATION_URL_ENCODED.headerName())) {

                                requestBodyStr = convertMapToUrlEncodedString(requestBodyMap);
                            }

                            if(requestBodyStr == null)
                                requestBodyStr = "";

                            requestBody = RequestBody.create(requestBodyStr, httpActionType);

                            if (!disableDebug)
                                Log.d(TAG + " Request Body " + functionName, requestBodyStr);
                        }

                        switch (connectorTransfer.getActionType().getValue()) {

                            case 2:
                                requestBuilder.post(requestBody);
                                break;

                            case 3:
                                requestBuilder.put(requestBody);
                                break;

                            case 4:
                                requestBuilder.delete(requestBody);
                                break;
                        }
                    }

                    OkHttpClient.Builder builder = AppOkHttpClient.GetOkHttpClientBuilder();
                    builder.addNetworkInterceptor(chain -> {

                        Request originalRequest = chain.request();

                        if (originalRequest.body() == null) {
                            return chain.proceed(originalRequest);
                        }

                        Request progressRequest = originalRequest.newBuilder()
                                .method(originalRequest.method(), new CountingRequestBody(originalRequest.body(), new ProgressListener() {

                                    long prevProgress = -1;

                                    @Override
                                    public void onRequestProgress(long bytesTransferred, long contentLength, boolean done) {

                                        long progress = bytesTransferred > contentLength ? 100 : (contentLength <= 0 || done ? 100 : Math.round((((double) bytesTransferred / contentLength) * (double) 100)));

                                        if(progress != prevProgress) {

                                            onProgressUpdate(progress, PROGRESS_UPLOAD, bytesTransferred, contentLength);
                                            prevProgress = progress;
                                        }
                                    }
                                }))
                                .build();

                        return chain.proceed(progressRequest);
                    });

                    builder.addNetworkInterceptor(chain -> {

                        Response originalResponse = chain.proceed(chain.request());

                        if(originalResponse.body() == null) {
                            return originalResponse;
                        }

                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), new ProgressListener() {

                                    boolean firstUpdate = true;
                                    long prevProgress = -1;

                                    @Override
                                    public void onRequestProgress(long bytesTransferred, long contentLength, boolean done) {

                                        if(firstUpdate) {

                                            firstUpdate = false;

                                            if(done) {
                                                onProgressUpdate(100L, PROGRESS_DOWNLOAD, bytesTransferred, contentLength);
                                            }
                                            return;
                                        }

                                        long progress = bytesTransferred > contentLength ? 100 : (contentLength <= 0 || done ? 100 : Math.round((((double) bytesTransferred / (double) contentLength) * 100)));

                                        if(progress != prevProgress) {

                                            onProgressUpdate(progress, PROGRESS_DOWNLOAD, bytesTransferred, contentLength);
                                            prevProgress = progress;
                                        }
                                    }
                                }))
                                .build();
                    });

                    Request request = requestBuilder.build();
                    Response response = builder.build().newCall(request).execute();

                    responseCode = response.code();
                    responseString = response.body().string();
                }
                else {

                    try {

                        //Load response from Assets
                        responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
                        InputStream responseStream = mContext.getAssets().open(simulatedResponseFile);

                        if(functionName == null) {

                            functionName = simulatedResponseFile;
                            String[] functionNameArr = functionName.split("\\.");
                            if(functionNameArr.length > 1) {
                                functionName = "";
                                for (int k = 0; k < functionNameArr.length - 1; k++) {
                                    functionName += (" " + functionNameArr[k]);
                                }
                            }
                        }

                        try {

                            responseString = convertStreamToString(responseStream);
                        }
                        catch (Exception e) {

                            processException = e;
                        }
                    }
                    catch (Exception e) {

                        processException = new Exception("Please pass valid 'simulated response file' name in Transfer Object to load form Assets!");
                    }
                }
            }
            catch (Exception e) {

                processException = e;
            }

            connectorTransfer.setFunctionName(functionName);

            return responseString;
        }

        @Override
        protected void onProgressUpdate(Long... values) {

            if(!disableDebug && values != null)
                Log.d(TAG + " Progress " + functionName, ""+ Arrays.toString(values));

            if(serviceResponse != null)
                serviceResponse.onProgressUpdate(values);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String response) {

            String errorCode = "CSeX";
            String errorMsg = "Server not reachable";
            if(mContext != null) {
                errorMsg = mContext.getString(R.string.server_not_reachable);
            }

            if(!disableDebug)
                Log.d(TAG + " Response Code " + functionName, ""+ responseCode);

            if(!disableDebug)
                Log.d(TAG + " Response Body " + functionName, ""+ response);

            if(responseCode == 403 || responseCode == 401 ) {

                 if(SessionContext.getInstance().getAccessToken() == null || SessionContext.getInstance().getAccessToken().isEmpty()) {

                     serviceResponse.onError(responseCode, errorCode, mContext.getResources().getString(R.string.un_authorized));
//                     showPhoneNumberActivity();
                     return;
                 }

                if(!SessionContext.getInstance().isIstokenrefreshhhpo()) {

                    processTokenRefresh();
                }
                else {

                    serviceResponse.onError(responseCode, errorCode, mContext.getString(R.string.error_while_processing));
                }
            }
            else if(responseCode == 404 && response != null && response.contains("MessageDetail")) {

                errorMsg = mContext.getString(R.string.error_while_processing);

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    errorMsg = jsonObject.optString("MessageDetail", errorMsg);
                }
                catch (Exception e) {

                    e.printStackTrace();
                }

                serviceResponse.onError(responseCode, errorCode, errorMsg);
            }
            else if(responseCode == 500 && response != null) {

                errorMsg = mContext.getString(R.string.error_while_processing);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if(response.contains("ExceptionMessage")) {

                        errorMsg = jsonObject.optString("ExceptionMessage", errorMsg);
                    }
                    else if(response.contains("Message")) {

                        errorMsg = jsonObject.optString("Message", errorMsg);
                    }
                }
                catch (Exception e) {

                    e.printStackTrace();
                }

                serviceResponse.onError(responseCode, errorCode, errorMsg);
            }
            else if(response != null && /*!response.isEmpty() &&*/ !response.equals("[]") && !response.equalsIgnoreCase("null")) {

                serviceResponse.onSuccess(responseCode, response,false);
            }
            else {

                if(response != null && response.equalsIgnoreCase("[]")) {

                    serviceResponse.onSuccess(responseCode, response,false);
                    return;
                }

                if(processException != null) {

                    if(responseCode == HTTP_OK)
                        responseCode = HTTP_INTERNAL_ERROR;

                    processException.printStackTrace();
                    errorMsg = processException.getLocalizedMessage();

                    if(errorMsg.startsWith("Certificate pinning failure")) {
                        errorMsg = "Certificate pinning failure";
                    }
                    else if(errorMsg.startsWith("failed to connect to") || errorMsg.startsWith("Unable to resolve host") ||
                            processException instanceof UnknownHostException ||
                            processException instanceof SSLException ||
                            processException instanceof ConnectException) {

                        errorMsg = mContext.getString(R.string.server_not_reachable);
                    }

                    serviceResponse.onError(responseCode, errorCode, errorMsg);
                }
                else {

                    serviceResponse.onError(responseCode, errorCode, mContext.getString(R.string.server_connection_error));
                }
            }
        }

        private void processTokenRefresh() {

            try {

                SessionContext.getInstance().setIstokenrefreshhhpo(true);

                Commons.refreshToken(mContext, new ServiceResponse() {

                    @Override
                    public void onSuccess(int responseCode, Object response,boolean istokenrefresh) {

                        if (responseCode == 200 && response != null) {

                            try {

                                JSONObject obj = new JSONObject(response.toString());

                                if (obj.has("token") && obj.has("refreshToken") ) {

                                    SessionContext.getInstance().setAccessToken(obj.getString("token"));
                                    SessionContext.getInstance().setRefreshToken(obj.getString("refreshToken"));
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                            SessionContext.getInstance().setIstokenrefreshhhpo(false);

                            serviceResponse.onSuccess(responseCode, response,true);
                        }
                        else/* if(responseCode == 500 || response == null)*/ {

                            serviceResponse.onError(responseCode,"888" ,"Unknown");
                        }
                    }

                    @Override
                    public void onProgressUpdate(Long... progress) {

                    }

                    /*private void reVerifyOtp() {

                        Commons.getInstance(mContext).reverifyOtp(new ServiceResponse() {

                            @Override
                            public void onSuccess(int responseCode, Object response, boolean istokenrefresh) {

                                if (responseCode == 200 && response != null) {

                                    TokenModel tokenModel = new Gson().fromJson(response.toString(), TokenModel.class);

                                    if (tokenModel != null) {

                                        SessionContext.getInstance().setTokenModel(tokenModel);
                                        SessionContext.getInstance().setRefreshTokenModel(tokenModel.Refresh_Token); //Added for future use

                                        SessionContext.getInstance().setIstokenrefreshhhpo(false);

                                        serviceResponse.onSuccess(responseCode, response, true);
                                    }
                                    else {

                                        serviceResponse.onError(HTTP_UNAVAILABLE, "-101", "Invalid Token Model");
                                    }
                                }
                                else {

                                    Intent intent = new Intent(mContext, PhoneNumberActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    mContext.startActivity(intent);
                                }
                            }

                            @Override
                            public void onProgressUpdate(Long... progress) {

                            }

                            @Override
                            public void onProgressUpdate(Object update) {

                            }

                            @Override
                            public void onError(int responseCode, String errorCode, String errorMessage) {

                                serviceResponse.onError(responseCode, errorCode, errorMessage);
                                SessionContext.getInstance().setIstokenrefreshhhpo(false);
                            }
                        });
                    }*/

                    @Override
                    public void onError(int responseCode, String errorCode, String errorMessage) {


                        serviceResponse.onError(responseCode, errorCode, errorMessage);
                        SessionContext.getInstance().setIstokenrefreshhhpo(false);
                    }
                });
            }
            catch (Exception e) {

                e.printStackTrace();
            }
        }


        private String convertMapToUrlEncodedString(Map<String, Object> reqParams) {

            String reqBody = "";

            try {

                StringBuilder result = new StringBuilder();
                boolean first = true;

                for (Map.Entry<String, Object> entry : reqParams.entrySet()) {

                    String key = entry.getKey();
                    String value = entry.getValue().toString();

                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(key, "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(value, "UTF-8"));
                }

                reqBody = result.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return reqBody;
        }

        public String convertStreamToString(final InputStream inputStream) {

            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();

            try (Reader in = new InputStreamReader(inputStream, Charsets.UTF_8)) {

                for (;;) {

                    int rsz = in.read(buffer, 0, buffer.length);
                    if (rsz < 0)
                        break;

                    out.append(buffer, 0, rsz);
                }
            }
            catch (Exception e) {

                e.printStackTrace();
            }

            return out.toString();
        }
    }

    public static class CountingRequestBody extends RequestBody {

        protected RequestBody delegate;
        protected ProgressListener progressListener;

        protected CountingSink countingSink;

        public CountingRequestBody(RequestBody delegate, ProgressListener progressListener) {

            this.delegate = delegate;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {

            try {

                return delegate.contentLength();
            }
            catch (IOException e) {

                e.printStackTrace();
            }

            return -1;
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {

            countingSink = new CountingSink(sink);
            BufferedSink bufferedSink = Okio.buffer(countingSink);

            delegate.writeTo(bufferedSink);

            bufferedSink.flush();
        }

        protected final class CountingSink extends ForwardingSink {

            private long bytesWritten = 0;

            public CountingSink(Sink delegate) {
                super(delegate);
            }

            @Override
            public void write(@NonNull Buffer source, long byteCount) throws IOException {

                super.write(source, byteCount);

                bytesWritten += (byteCount != -1 ? byteCount : 0);
                progressListener.onRequestProgress(bytesWritten, contentLength(), byteCount == -1);
            }
        }
    }

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {

            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        @NonNull
        public BufferedSource source() {

            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {

            return new ForwardingSource(source) {

                long totalBytesRead = 0L;

                @Override
                public long read(@NonNull Buffer sink, long byteCount) throws IOException {

                    long bytesRead = super.read(sink, byteCount);
                    totalBytesRead += (bytesRead != -1 ? bytesRead : 0);
                    progressListener.onRequestProgress(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    private interface ProgressListener {
        void onRequestProgress(long bytesTransferred, long contentLength, boolean done);
    }
}