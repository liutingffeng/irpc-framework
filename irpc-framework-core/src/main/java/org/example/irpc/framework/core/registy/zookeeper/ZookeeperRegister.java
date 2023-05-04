package org.example.irpc.framework.core.registy.zookeeper;

import com.alibaba.fastjson.JSON;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.example.irpc.framework.core.common.event.IRpcEvent;
import org.example.irpc.framework.core.common.event.IRpcListenerLoader;
import org.example.irpc.framework.core.common.event.IRpcNodeChangeEvent;
import org.example.irpc.framework.core.common.event.IRpcUpdateEvent;
import org.example.irpc.framework.core.common.event.data.URLChangeWrapper;
import org.example.irpc.framework.core.registy.RegistryService;
import org.example.irpc.framework.core.registy.URL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;
import static org.example.irpc.framework.core.common.cache.CommonServerCache.SERVER_CONFIG;

public class ZookeeperRegister extends AbstractRegister {

    private AbstractZookeeperClient zkClient;

    private String ROOT = "/irpc";

    private String getProviderPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/provider/" + url.getParameters().get("host") + ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host") + ":";
    }

    public ZookeeperRegister() {
        String registryAddr = CLIENT_CONFIG!= null ? CLIENT_CONFIG.getRegisterAddr() : SERVER_CONFIG.getRegisterAddr();
        this.zkClient = new CuratorZookeeperClient(registryAddr);
    }
    public ZookeeperRegister(String address) {
        this.zkClient = new CuratorZookeeperClient(address);
    }

    @Override
    public void register(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildProviderUrlStr(url);
        if (!zkClient.existNode(getProviderPath(url))) {
            zkClient.createTemporaryData(getProviderPath(url) ,urlStr);
        } else {
            zkClient.deleteNode(getProviderPath(url));
            zkClient.createTemporaryData(getProviderPath(url) ,urlStr);
        }
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if (!this.zkClient.existNode(ROOT)) {
            zkClient.createPersistentData(ROOT, "");
        }
        String urlStr = URL.buildConsumerUrlStr(url);
        if (!zkClient.existNode(getConsumerPath(url))) {
            zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        } else {
            zkClient.deleteNode(getConsumerPath(url));
            zkClient.createTemporarySeqData(getConsumerPath(url), urlStr);
        }
        super.subscribe(url);
    }

    @Override
    public void doAfterSubscribe(URL url) {
        //监听是否有新的服务注册
        //监听是否有新的服务注册
        String servicePath = url.getParameters().get("servicePath");
        String newServerNodePath = ROOT + "/" + servicePath;
        watchChildNodeData(newServerNodePath);
        String providerIpStrJson = url.getParameters().get("providerIps");
        List<String> providerIpList = JSON.parseObject(providerIpStrJson, List.class);
        for (String providerIp : providerIpList) {
            this.watchNodeDataChange(ROOT + "/" + servicePath + "/" + providerIp);
        }
    }

    /**
     * 订阅服务节点内部的数据变化
     *
     * @param newServerNodePath
     */
    public void watchNodeDataChange(String newServerNodePath) {
        zkClient.watchNodeData(newServerNodePath, new Watcher() {

            @Override
            public void process(WatchedEvent watchedEvent) {
                String path = watchedEvent.getPath();
                String nodeData = zkClient.getNodeData(path);
                nodeData = nodeData.replace(";","/");
                ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(nodeData);
                IRpcEvent iRpcEvent = new IRpcNodeChangeEvent(providerNodeInfo);
                IRpcListenerLoader.sendEvent(iRpcEvent);
                watchNodeDataChange(newServerNodePath);
            }
        });
    }

    public void watchChildNodeData(String newServerNodePath){
        zkClient.watchChildNodeData(newServerNodePath, (WatchedEvent watchedEvent) -> {
            System.out.println(watchedEvent);
            String path = watchedEvent.getPath();
            List<String> childrenDataList = zkClient.getChildrenData(path);
            URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
            urlChangeWrapper.setProviderUrl(childrenDataList);
            urlChangeWrapper.setServiceName(path.split("/")[2]);
            //自定义的一套事件监听组件
            IRpcEvent iRpcEvent = new IRpcUpdateEvent(urlChangeWrapper);
            IRpcListenerLoader.sendEvent(iRpcEvent);
            //收到回调之后在注册一次监听，这样能保证一直都收到消息
            watchChildNodeData(path);
        });
    }

    @Override
    public void doBeforeSubscribe(URL url) {

    }

    @Override
    public List<String> getProviderIps(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        return nodeDataList;
    }

    @Override
    public Map<String, String> getServiceWeightMap(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT + "/" + serviceName + "/provider");
        Map<String, String> result = new HashMap<>();
        for (String ipAndHost : nodeDataList) {
            String childData = this.zkClient.getNodeData(ROOT + "/" + serviceName + "/provider/" + ipAndHost);
            result.put(ipAndHost, childData);
        }
        return result;
    }
}
