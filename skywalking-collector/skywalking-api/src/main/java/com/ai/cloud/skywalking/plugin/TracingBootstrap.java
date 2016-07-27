package com.ai.cloud.skywalking.plugin;

import com.ai.cloud.skywalking.logging.LogManager;
import com.ai.cloud.skywalking.logging.Logger;
import com.ai.cloud.skywalking.plugin.exception.PluginException;
import com.ai.cloud.skywalking.plugin.interceptor.enhance.ClassEnhancePluginDefine;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * 替代应用函数的main函数入口，确保在程序入口处运行 <br/>
 * 用于替代-javaagent的另一种模式 <br/>
 * 主要用于插件的本地化调试与运行<br/>
 *
 * @author wusheng
 */
public class TracingBootstrap {
    private static Logger logger = LogManager.getLogger(TracingBootstrap.class);

    private TracingBootstrap() {
    }

    public static void main(String[] args)
            throws NotFoundException, PluginException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (args.length == 0) {
            throw new RuntimeException("bootstrap failure. need args[0] to be main class.");
        }

        PluginBootstrap bootstrap = new PluginBootstrap();
        Map<String, ClassEnhancePluginDefine> pluginDefineMap = bootstrap.loadPlugins();

        for (Map.Entry<String, ClassEnhancePluginDefine> entry : pluginDefineMap.entrySet()) {
            CtClass ctClass = ClassPool.getDefault().get(entry.getKey());
            entry.getValue().enhance(ctClass);
        }

        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);


        Class.forName(args[0]).getMethod("main", String[].class).invoke(null, new Object[] {newArgs});
    }
}
