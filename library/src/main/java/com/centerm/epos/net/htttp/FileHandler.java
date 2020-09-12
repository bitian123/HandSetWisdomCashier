package com.centerm.epos.net.htttp;

import java.io.File;

/**
 * author:wanliang527</br>
 * date:2016/12/6</br>
 */

public interface FileHandler {

    void onStart(String url);

    void onProgress(long bytesWritten, long tSize);

    void onSuccess(File file);

    void onFailure(int code, Throwable throwable);

    void onCanceled();
}
