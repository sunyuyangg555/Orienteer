package org.orienteer.core.component.widget.document.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.model.AbstractCheckBoxModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractSaveCommand;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.component.property.BooleanEditPanel;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.widget.oclass.OClassSecurityWidget;
import org.orienteer.core.event.ActionPerformedEvent;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;

import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;
import ru.ydn.wicket.wicketorientdb.behavior.DisableIfDocumentNotSavedBehavior;
import ru.ydn.wicket.wicketorientdb.behavior.SecurityBehavior;
import ru.ydn.wicket.wicketorientdb.model.EnumNamingModel;
import ru.ydn.wicket.wicketorientdb.model.JavaSortableDataProvider;
import ru.ydn.wicket.wicketorientdb.security.OSecurityHelper;
import ru.ydn.wicket.wicketorientdb.security.OrientPermission;
import ru.ydn.wicket.wicketorientdb.security.RequiredOrientResource;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Widget to configure security on ORole
 */
@Widget(id="role-security", domain="document", tab="security", order=30, autoEnable=true, selector="ORole")
public class ORoleSecurityWidget extends AbstractWidget<ODocument> {
	
	private class SecurityRightsColumn extends AbstractColumn<String, String>
	{
		private final OrientPermission permission;
		public SecurityRightsColumn(OrientPermission permission)
		{
			super(new EnumNamingModel<OrientPermission>(permission));
			this.permission = permission;
		}

		@Override
		public void populateItem(Item<ICellPopulator<String>> cellItem,
				String componentId, IModel<String> rowModel) {
			cellItem.add(new BooleanEditPanel(componentId, getSecurityRightsModel(rowModel)));
		}
		
		protected IModel<Boolean> getSecurityRightsModel(final IModel<String> rowModel)
		{
			return new AbstractCheckBoxModel() {
				
				@SuppressWarnings("deprecation")
				@Override
				public void unselect() {
					ORole oRole = roleModel.getObject();
					oRole.revoke(rowModel.getObject(), permission.getPermissionFlag());
					oRole.save();
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void select() {
					ORole oRole = roleModel.getObject();
					oRole.grant(rowModel.getObject(), permission.getPermissionFlag());
					oRole.save();
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public boolean isSelected() {
					ORole oRole = roleModel.getObject();
					return oRole.allow(rowModel.getObject(), permission.getPermissionFlag());
				}
				
			};
		}
	}
	
	private IModel<ORole> roleModel = new LoadableDetachableModel<ORole>() {

		@Override
		protected ORole load() {
			return getDatabase().getMetadata().getSecurity().getRole(ORoleSecurityWidget.this.getModelObject());
		}
	};
	
	public ORoleSecurityWidget(String id, IModel<ODocument> model,
			IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		Form<OClass> sForm = new Form<OClass>("form");
		sForm.add(new SecurityBehavior(model, Component.ENABLE, OrientPermission.UPDATE));
		
		List<IColumn<String, String>> sColumns = new ArrayList<IColumn<String, String>>();
		sColumns.add(new PropertyColumn<String, String>(new ResourceModel("orule"), ""));
		sColumns.add(new SecurityRightsColumn(OrientPermission.CREATE));
		sColumns.add(new SecurityRightsColumn(OrientPermission.READ));
		sColumns.add(new SecurityRightsColumn(OrientPermission.UPDATE));
		sColumns.add(new SecurityRightsColumn(OrientPermission.DELETE));
		
		JavaSortableDataProvider<String, String> provider = new JavaSortableDataProvider<String, String>(new PropertyModel<Collection<String>>(this, "ruleSet"));
		OrienteerDataTable<String, String> sTable = new OrienteerDataTable<String, String>("table", sColumns, provider ,20);
		Command<String> saveCommand = new AbstractSaveCommand<String>(sTable, null);
		sTable.addCommand(saveCommand);
		sForm.add(sTable);
		add(sForm);
		add(DisableIfDocumentNotSavedBehavior.INSTANCE);
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public Collection<String> getRuleSet() {
		ORole role = roleModel.getObject();
		return role!=null?role.getRules().keySet():Collections.EMPTY_SET;
	}

	@Override
	protected FAIcon newIcon(String id) {
		return new FAIcon(id, FAIconType.shield);
	}

	@Override
	protected IModel<String> getTitleModel() {
		return new ResourceModel("class.security");
	}
	
	@Override
	protected String getWidgetStyleClass() {
		return "strict";
	}
	
	@Override
	public void onActionPerformed(ActionPerformedEvent<?> event,
			IEvent<?> wicketEvent) {
		if(event.ofType(ODocument.class) && event.getCommand().isChangingModel() 
				&& event.isAjax() && ((ODocument)event.getObject()).getSchemaClass().isSubClassOf("ORole")) {
			event.getTarget().add(this);
			wicketEvent.dontBroadcastDeeper();
		}
	}

}
