package io.trygvis.jbpm.service;

public class AppService {
	public void installApp(AppInstance instance) {
        System.out.println("AppService.installApp");
    }

	public void restartApp(AppInstance instance) {
        System.out.println("AppService.restartApp");
    }

	public void waitForUp(AppInstance instance) {
        System.out.println("AppService.waitForAppUp");
    }
}
