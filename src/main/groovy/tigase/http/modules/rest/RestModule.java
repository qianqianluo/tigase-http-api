/*
 * Tigase HTTP API
 * Copyright (C) 2004-2014 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.http.modules.rest;

import tigase.http.DeploymentInfo;
import tigase.http.HttpMessageReceiver;
import tigase.http.ServletInfo;
import tigase.http.modules.AbstractModule;
import tigase.http.util.StaticFileServlet;
import tigase.kernel.beans.Bean;
import tigase.kernel.beans.config.ConfigField;
import tigase.kernel.beans.selector.ConfigType;
import tigase.kernel.beans.selector.ConfigTypeEnum;
import tigase.kernel.core.BeanConfig;
import tigase.kernel.core.Kernel;
import tigase.stats.StatisticHolder;
import tigase.stats.StatisticHolderImpl;
import tigase.stats.StatisticsList;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Bean(name = "rest", parent = HttpMessageReceiver.class, active = true)
@ConfigType({ConfigTypeEnum.DefaultMode, ConfigTypeEnum.SessionManagerMode, ConfigTypeEnum.ConnectionManagersMode, ConfigTypeEnum.ComponentMode})
public class RestModule extends AbstractModule {
	
	private static final Logger log = Logger.getLogger(RestModule.class.getCanonicalName());
	
	private static final String DEF_SCRIPTS_DIR_VAL = "scripts/rest";
	
	private static final String SCRIPTS_DIR_KEY = "rest-scripts-dir";

	private final ReloadHandlersCmd reloadHandlersCmd = new ReloadHandlersCmd(this);

	private DeploymentInfo httpDeployment = null;

	@ConfigField(desc = "Scripts directory", alias = SCRIPTS_DIR_KEY)
	private String scriptsDir = DEF_SCRIPTS_DIR_VAL;

	private List<RestServlet> restServlets = new ArrayList<RestServlet>();
		
	private static final ConcurrentHashMap<String,StatisticHolder> stats = new ConcurrentHashMap<String, StatisticHolder>();
	
	@Override
	public void everyHour() {
		for (StatisticHolder holder : stats.values()) {
			holder.everyHour();
		} 		
	}
	
	@Override
	public void everyMinute() {
		for (StatisticHolder holder : stats.values()) {
			holder.everyMinute();
		} 	
	}
	
	@Override
	public void everySecond() {
		for (StatisticHolder holder : stats.values()) {
			holder.everySecond();
		} 		
	}	

	@Override
	public String getDescription() {
		return "REST support - handles HTTP REST access using scripts";
	}
	
	@Override
	public void start() {
		if (httpDeployment != null) {
			stop();
		}

		super.start();
		httpDeployment = httpServer.deployment().setClassLoader(this.getClass().getClassLoader())
				.setContextPath(contextPath).setService(new tigase.http.ServiceImpl(this)).setDeploymentName("REST API")
				.setDeploymentDescription(getDescription());
		if (vhosts != null) {
			httpDeployment.setVHosts(vhosts);
		}
		File scriptsDirFile = new File(scriptsDir);
		File[] scriptDirFiles = scriptsDirFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && !"static".equals(file.getName());
			}
		});

		if (scriptDirFiles != null) {
			for (File dirFile : scriptDirFiles) {
				try {
					startRestServletForDirectory(httpDeployment, dirFile);
				} catch (IOException ex) {
					log.log(Level.FINE, "Exception while scanning for scripts to load", ex);
				}
			}
		}
		
		try {
			ServletInfo servletInfo = httpServer.servlet("RestServlet", RestExtServlet.class);
			servletInfo.addInitParam(RestServlet.REST_MODULE_KEY, uuid)
					.addInitParam(RestServlet.SCRIPTS_DIR_KEY, scriptsDirFile.getCanonicalPath())
					.addMapping("/");
			httpDeployment.addServlets(servletInfo);
		} catch (IOException ex) {
			log.log(Level.FINE, "Exception while scanning for scripts to load", ex);
		}
		
		
		ServletInfo servletInfo = httpServer.servlet("StaticServlet", StaticFileServlet.class);
		servletInfo.addInitParam(StaticFileServlet.DIRECTORY_KEY, new File(scriptsDirFile, "static").getAbsolutePath())
				.addMapping("/static/*");
		httpDeployment.addServlets(servletInfo);		
		
		httpServer.deploy(httpDeployment);
	}

	@Override
	public void stop() {
		if (httpDeployment != null) { 
			httpServer.undeploy(httpDeployment);
			httpDeployment = null;
		}
		Kernel kernel = getKernel(uuid);
		Iterator<BeanConfig> it = kernel.getDependencyManager().getBeanConfigs().iterator();
		while (it.hasNext()) {
			BeanConfig bc = it.next();
			if (bc.getState() == BeanConfig.State.initialized) {
				try {
					kernel.unregister(bc.getBeanName());
				} catch (Exception ex) {
					log.log(Level.WARNING, "Could not unregister bean!", ex);
				}
			}
		}
		restServlets = new ArrayList<RestServlet>();
		super.stop();
	}

	@Override
	public void getStatistics(String compName, StatisticsList list) {
		for (StatisticHolder holder : stats.values()) {
			holder.getStatistics(compName, list);
		} 
	}			
	
	public void executedIn(String path, long executionTime) {
		StatisticHolder holder = stats.get(path);
		if (holder == null) {
			StatisticHolder tmp = new StatisticHolderImpl();
			tmp.setStatisticsPrefix(getName() + ", path=" + path);
			holder = stats.putIfAbsent(path, tmp);
			if (holder == null)
				holder = tmp;
		}
		holder.statisticExecutedIn(executionTime);
	}
	
	public void statisticExecutedIn(long executionTime) {
		
	}
	
	protected void registerRestServlet(RestServlet servlet) {
		restServlets.add(servlet);
	}
	
	protected List<? extends RestServlet> getRestServlets() {
		return restServlets;
	}
	
    private void startRestServletForDirectory(DeploymentInfo httpDeployment, File scriptsDirFile) 
			throws IOException {
        File[] scriptFiles = getGroovyFiles(scriptsDirFile);

        if (scriptFiles != null) {
			ServletInfo servletInfo = httpServer.servlet("RestServlet", RestExtServlet.class);
			servletInfo.addInitParam(RestServlet.REST_MODULE_KEY, uuid)
					.addInitParam(RestServlet.SCRIPTS_DIR_KEY, scriptsDirFile.getCanonicalPath())
					.addInitParam("mapping", "/" + scriptsDirFile.getName() + "/*")
					.addMapping("/" + scriptsDirFile.getName() + "/*");
			httpDeployment.addServlets(servletInfo);
        }
    }	

	public static File[] getGroovyFiles( File scriptsDirFile) {
		return scriptsDirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith("groovy");
            }
        });
	}

	public Kernel getKernel() {
		return getKernel(uuid);
	}

	@Override
	public void initialize() {
		super.initialize();
		commandManager.registerCmd(reloadHandlersCmd);
	}
}
