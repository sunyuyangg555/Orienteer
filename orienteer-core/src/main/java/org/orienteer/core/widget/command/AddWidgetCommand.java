package org.orienteer.core.widget.command;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.BootstrapSize;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.component.command.modal.ImportDialogPanel;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.DashboardPanel;
import org.orienteer.core.widget.IWidgetType;
import org.orienteer.core.widget.command.modal.AddWidgetDialog;

import com.google.common.escape.CharEscaper;
import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Command for {@link DashboardPanel} to add new widget
 *
 * @param <T> the type of main object for a {@link DashboardPanel}
 */
public class AddWidgetCommand<T> extends AbstractModalWindowCommand<ODocument> {
	
	public AddWidgetCommand(String id, IModel<ODocument> dashboardDocumentModel) {
		super(id, "command.add.widget", dashboardDocumentModel);
		setIcon(FAIconType.plus_circle);
		setBootstrapType(BootstrapType.SUCCESS);
		setBootstrapSize(BootstrapSize.EXTRA_SMALL);
	}

	@Override
	protected void initializeContent(final ModalWindow modal) {
		modal.setTitle(new ResourceModel("command.add.widget"));
		modal.setContent(new AddWidgetDialog<T>(modal.getContentId()) {

			@Override
			protected void onSelectWidgetType(IWidgetType<T> type,
					AjaxRequestTarget target) {
				modal.close(target);
				DashboardPanel<T> dashboard = getDashboardPanel();
				AbstractWidget<T> widget = dashboard.addWidget(type);
				dashboard.getDashboardSupport().ajaxAddWidget(widget, target);
			}
		});
		modal.setAutoSize(true);
		modal.setMinimalWidth(300);
	}

}
