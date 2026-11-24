package com.sarthi.Sms.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sarthi.Sms.job.FwtJob;
import com.sarthi.Sms.job.MacroJob;
import com.sarthi.Sms.job.TensileJob;
import com.sarthi.Sms.job.ChemJob;
@Configuration
// Rail-mill lab-result jobs: off unless railmill.jobs.enabled=true (keeps SARTHI unaffected until rail-mill goes live)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "railmill.jobs.enabled", havingValue = "true")
public class QuartzSchedulerConfig {

    // FWT Job
    @Bean
    public JobDetail fwtJobDetail() {
        return JobBuilder.newJob(FwtJob.class)
            .withIdentity("fwtJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger fwtTrigger() {
        return TriggerBuilder.newTrigger()
            .forJob(fwtJobDetail())
            .withIdentity("fwtTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?")) // Every day at 1 AM
            .build();
    }

    // MACRO Job
    @Bean
    public JobDetail macroJobDetail() {
        return JobBuilder.newJob(MacroJob.class)
            .withIdentity("macroJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger macroTrigger() {
        return TriggerBuilder.newTrigger()
            .forJob(macroJobDetail())
            .withIdentity("macroTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 10 1 * * ?")) // Every day at 1:10 AM
            .build();
    }

    // CHEM Job
    @Bean
    public JobDetail chemJobDetail() {
        return JobBuilder.newJob(ChemJob.class)
            .withIdentity("chemJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger chemTrigger() {
        return TriggerBuilder.newTrigger()
            .forJob(chemJobDetail())
            .withIdentity("chemTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 20 1 * * ?")) // Every day at 1:20 AM
            .build();
    }

    @Bean
    public JobDetail tensileJobDetail() {
        return JobBuilder.newJob(TensileJob.class)
            .withIdentity("tensileJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger tensileTrigger(JobDetail tensileJobDetail) {
        return TriggerBuilder.newTrigger()
            .forJob(tensileJobDetail)
            .withIdentity("tensileTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 4 * * ?")) // every day at 4 AM
            .build();
    }
}
