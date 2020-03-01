package com.xirpc.registry.xicp;

import com.summerframework.core.logger.LogFactory;
import com.xicp.WatchedEvent;
import com.xicp.XiCP;
import com.xirpc.common.URL;
import com.xirpc.common.UrlUtil;
import com.xirpc.registry.NotifyListener;
import com.xirpc.registry.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: XiCPRegistry
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class XiCPRegistry implements Registry, NotifyListener {

    private static final LogFactory logger = new LogFactory(XiCPRegistry.class);

    protected final String REGISTRY_PREFIX = "/XiRpc";
    protected final String REGISTRY_PROVIDER_PREFIX = REGISTRY_PREFIX + "/provider";
    protected final String REGISTRY_CONSUMER_PREFIX = REGISTRY_PREFIX + "/consumer";
    private XiCP xc;
    private Set<String> consumerClassList;
    private Map<String, Set<URL>> consumerMap = new HashMap<>();

    public XiCPRegistry(String connectString, Set<String> consumerClassList) throws Exception {
        this.xc = new XiCP(connectString, this);
        this.consumerClassList = consumerClassList;
        initRegistry();
    }

    private void initRegistry() throws Exception {
        logger.info("initRegistry...");
        if (!xc.exists("/", true)) {
            xc.create("/", "/".getBytes(), false, false);
        }
        if (!xc.exists(REGISTRY_PREFIX, true)) {
            xc.create(REGISTRY_PREFIX, REGISTRY_PREFIX.getBytes(), false, false);
        }
        if (!xc.exists(REGISTRY_PROVIDER_PREFIX, true)) {
            xc.create(REGISTRY_PROVIDER_PREFIX, REGISTRY_PROVIDER_PREFIX.getBytes(), false, false);
        }
        if (!xc.exists(REGISTRY_CONSUMER_PREFIX, true)) {
            xc.create(REGISTRY_CONSUMER_PREFIX, REGISTRY_CONSUMER_PREFIX.getBytes(), false, false);
        }
    }

    @Override
    public void register(URL url) {
        try {
            String serviceInterface = REGISTRY_PROVIDER_PREFIX + "/" + url.getPath();
            if (!xc.exists(serviceInterface, true)) {
                xc.create(serviceInterface, serviceInterface.getBytes(), false, false);
            }
            String service = serviceInterface + "/" + url.toString();
            if (!xc.exists(service, true)) {
                xc.create(service, service.getBytes(), true, false);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(URL url) {
        try {
            String provider = REGISTRY_PROVIDER_PREFIX + "/" + url.getPath();
            List<String> children = xc.getChildren(provider, true);
            consumerMap.put(url.getPath(), UrlUtil.strToUrls(children));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(List<URL> urls) {
        reWatch();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            logger.info("XiCPRegistry process...");
            reWatch();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void reWatch() {
        try {
            for (String consumer : consumerClassList) {
                consumer = REGISTRY_PROVIDER_PREFIX + "/" + consumer;
                List<String> children = xc.getChildren(consumer, true);
                consumerMap.put(consumer, UrlUtil.strToUrls(children));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Set<URL> getRemoteServiceList(String key) {
        return consumerMap.get(key);
    }
}
