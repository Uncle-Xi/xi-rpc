package com.xirpc.registry;

import com.xicp.Watcher;
import com.xirpc.common.URL;

import java.util.List;

public interface NotifyListener extends Watcher {

    void notify(List<URL> urls);
}
