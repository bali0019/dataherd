package org.jas.plugins.utils.service.impl;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.service.IExecutionStatus;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jabali on 3/6/17.
 */
public class ExecutionStatusProvider implements IExecutionStatus {

    private List<ThreadPoolTaskExecutor> threadPoolExecutors;


    public ExecutionStatusProvider(List<ThreadPoolTaskExecutor> threadPoolExecutors) {
        this.threadPoolExecutors = threadPoolExecutors;
    }

    @Override
    public ScriptExecutionStatusEnum status() {
        Iterator<ThreadPoolTaskExecutor> executors = threadPoolExecutors.iterator();
        while (executors.hasNext()) {
            ThreadPoolTaskExecutor item = executors.next();
            if(item.getThreadPoolExecutor().isShutdown()) {
                executors.remove();
            }
        }

        if(threadPoolExecutors==null || threadPoolExecutors.isEmpty()) {
            return ScriptExecutionStatusEnum.COMPLETED;
        } else {
            return ScriptExecutionStatusEnum.EXECUTING;
        }


    }
}
