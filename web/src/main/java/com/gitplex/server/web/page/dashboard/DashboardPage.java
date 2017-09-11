package com.gitplex.server.web.page.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.web.ComponentRenderer;
import com.gitplex.server.web.component.projectlist.ProjectListPanel;
import com.gitplex.server.web.page.admin.systemsetting.SystemSettingPage;
import com.gitplex.server.web.page.group.GroupListPage;
import com.gitplex.server.web.page.group.NewGroupPage;
import com.gitplex.server.web.page.layout.LayoutPage;
import com.gitplex.server.web.page.project.NewProjectPage;
import com.gitplex.server.web.page.project.ProjectListPage;
import com.gitplex.server.web.page.user.NewUserPage;
import com.gitplex.server.web.page.user.UserListPage;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class DashboardPage extends LayoutPage {

	private static final int MAX_RECENT_PROJECTS = 10;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Link<Void> link = new BookmarkablePageLink<Void>("projects", ProjectListPage.class);
		link.add(new Label("count", GitPlex.getInstance(ProjectManager.class)
				.getAccessibleProjects(getLoginUser()).size()));
		add(link);
		
		add(new BookmarkablePageLink<Void>("addProject", NewProjectPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canCreateProjects());
			}
			
		});
		
		IModel<List<ProjectFacade>> recentVisitedProjectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

			@Override
			protected List<ProjectFacade> load() {
				List<ProjectFacade> projects = new ArrayList<>(GitPlex.getInstance(ProjectManager.class)
						.getAccessibleProjects(getLoginUser()));
				projects.sort(ProjectFacade::compareLastVisit);
				if (projects.size() > MAX_RECENT_PROJECTS)
					return projects.subList(0, MAX_RECENT_PROJECTS);
				else
					return projects;
			}
			
		};
		
		add(new ProjectListPanel("recentVisitedProjects", recentVisitedProjectsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() != null && !recentVisitedProjectsModel.getObject().isEmpty());
			}
			
		});
		
		IModel<List<ProjectFacade>> recentAddedProjectsModel = new LoadableDetachableModel<List<ProjectFacade>>() {

			@Override
			protected List<ProjectFacade> load() {
				List<ProjectFacade> projects = new ArrayList<>(GitPlex.getInstance(ProjectManager.class)
						.getAccessibleProjects(getLoginUser()));
				Map<String, Long> projectIds = GitPlex.getInstance(CacheManager.class).getProjectIds();
				projects.sort(new Comparator<ProjectFacade>() {

					@Override
					public int compare(ProjectFacade o1, ProjectFacade o2) {
						Long id1 = projectIds.get(o1.getName());
						Long id2 = projectIds.get(o2.getName());
						if (id1 == null)
							id1 = 0L;
						if (id2 == null)
							id2 = 0L;
						return (int)(id2 - id1);
					}
					
				});
				if (projects.size() > MAX_RECENT_PROJECTS)
					return projects.subList(0, MAX_RECENT_PROJECTS);
				else
					return projects;
			}
			
		};
		
		add(new ProjectListPanel("recentAddedProjects", recentAddedProjectsModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLoginUser() == null && !recentAddedProjectsModel.getObject().isEmpty());
			}
			
		});
		
		link = new BookmarkablePageLink<Void>("users", UserListPage.class) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		};
		link.add(new Label("count", GitPlex.getInstance(UserManager.class).findAll().size()));
		add(link);
		
		add(new BookmarkablePageLink<Void>("addUser", NewUserPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		link = new BookmarkablePageLink<Void>("groups", GroupListPage.class) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		};
		link.add(new Label("count", GitPlex.getInstance(GroupManager.class).findAll().size()));
		add(link);
		
		add(new BookmarkablePageLink<Void>("addGroup", NewGroupPage.class) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("administration", SystemSettingPage.class) {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.isAdministrator());
			}
			
		});
	}

	@Override
	protected List<ComponentRenderer> getBreadcrumbs() {
		return Lists.newArrayList(new ComponentRenderer() {

			@Override
			public Component render(String componentId) {
				return new Label(componentId, "Home");
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DashboardResourceReference()));
	}

}