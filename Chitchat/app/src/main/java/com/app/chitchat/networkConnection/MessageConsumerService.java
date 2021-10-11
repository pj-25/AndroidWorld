package com.app.chitchat.networkConnection;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;

public class MessageConsumerService extends JobService {

    public final static int JOB_ID = 0;
    public static boolean isRunning;

    MessageConsumer msgConsumer;

    @Override
    public boolean onStartJob(JobParameters params) {
        msgConsumer = new MessageConsumer(this);
        if(!msgConsumer.run()){
            jobFinished(params, false);
            return false;
        }
        isRunning = true;
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        msgConsumer.close();
        isRunning = false;
        return true;
    }

    public MessageConsumer getMsgConsumer() {
        return msgConsumer;
    }

    public void setMsgConsumer(MessageConsumer msgConsumer) {
        this.msgConsumer = msgConsumer;
    }

    public static boolean isJobServiceOn( Context context, int jobID ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        boolean hasBeenScheduled = false ;
        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() ==  jobID) {
                hasBeenScheduled = true ;
                break ;
            }
        }
        return hasBeenScheduled ;
    }

}
