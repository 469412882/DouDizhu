package com.lordcard.haoyun.permission;

import android.support.annotation.NonNull;


public interface PermissionListener {

    /**
     * 通过授权
     * @param permission
     */
    void permissionGranted(@NonNull String[] permission);

    /**
     * 拒绝授权
     * @param permission
     */
    void permissionDenied(@NonNull String[] permission);
}
