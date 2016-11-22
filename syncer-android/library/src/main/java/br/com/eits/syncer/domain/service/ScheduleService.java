package br.com.eits.syncer.domain.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import br.com.eits.syncer.application.ApplicationHolder;
import br.com.eits.syncer.application.background.SyncBackgroundService;
import br.com.eits.syncer.domain.entity.Revision;

/**
 *
 */
class ScheduleService
{
    /**
     *
     */
    private static final int SYNC_NOW_JOB_ID = -9999;

    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/
    /**
     *
     */
    private final JobScheduler jobScheduler;

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public ScheduleService()
    {
        //requeust the job scheduler
        this.jobScheduler = (JobScheduler) ApplicationHolder.CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    /*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
    /**
     *
     */
    public void requestSyncNow()
    {
        final ComponentName serviceName = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
        final JobInfo jobInfo = new JobInfo.Builder(SYNC_NOW_JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPersisted(true)
                .build();

        final int result = this.jobScheduler.schedule(jobInfo);

        if ( result != JobScheduler.RESULT_SUCCESS )
        {
            throw new IllegalArgumentException("Was not possible to schedule for sync.");
        }
        else
        {
            Log.d(ScheduleService.class.getSimpleName(), "Job scheduled successfully");
        }
    }

    /**
     *
     */
    public void requestSync( Revision revision )
    {
        final ComponentName serviceName = new ComponentName(ApplicationHolder.CONTEXT, SyncBackgroundService.class);
        final JobInfo jobInfo = new JobInfo.Builder(revision.getTime().intValue(), serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .setPersisted(true)
                .build();

        final int result = this.jobScheduler.schedule(jobInfo);

        if ( result != JobScheduler.RESULT_SUCCESS )
        {
            throw new IllegalArgumentException("Error doing the local operation. Was not possible to schedule for sync.");
        }
        else
        {
            Log.d(ScheduleService.class.getSimpleName(), "Job scheduled successfully for revision: "+revision.getTime());
        }
    }
}