package org.fbi.ltf.internal;

import org.fbi.linking.processor.ProcessorManagerService;
import org.fbi.ltf.server.taskserver.SchedulerManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

public class AppActivator implements BundleActivator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static BundleContext context;
    private ServerService serverService;
    private SchedulerManager schedulerManager;

    public static BundleContext getBundleContext() {
        return context;
    }

    public void start(BundleContext context) {
        AppActivator.context = context;

        startServer();
        startTasdkServer();

        ProcessorFactory factory = new ProcessorFactory();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("APPID", "CCBLTF");
        context.registerService(ProcessorManagerService.class.getName(), factory, properties);
        logger.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - Starting the CCB-LTF app bundle...." );
    }

    private void startServer() {
        this.serverService = new ServerService(getBundleContext());
        this.serverService.start();
    }
    private void startTasdkServer() {
        this.schedulerManager =new SchedulerManager();
        schedulerManager.start();
    }


    public void stop(BundleContext context) throws Exception {
        this.serverService.stop();
        this.schedulerManager.destroy();
        AppActivator.context = null;
        logger.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - Stopping the CCB-LTF app bundle...");
    }

}
