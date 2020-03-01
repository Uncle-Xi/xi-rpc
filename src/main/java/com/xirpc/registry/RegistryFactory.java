package com.xirpc.registry;

import java.net.URL;

public interface RegistryFactory {

    Registry getRegistry(URL url);
}
