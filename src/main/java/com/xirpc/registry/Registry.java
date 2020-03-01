package com.xirpc.registry;


import com.xirpc.common.URL;

public interface Registry {

    void register(URL url);

    void subscribe(URL url);
}
