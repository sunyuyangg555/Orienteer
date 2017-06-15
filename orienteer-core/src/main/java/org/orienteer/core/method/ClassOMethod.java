package org.orienteer.core.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.wicket.behavior.Behavior;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.method.methods.OClassOMethod;
import org.orienteer.core.method.methods.OClassTableOMethod;

/**
 * OClass method annotation for Java methods 
 * 
 * OMethod will display only if all filters passed
 * 
 * All filters should implement {@link IMethodFilter}
 * 
 * Java Class with this method SHOULD be named equals same OClass 
 * 
 * That method should have only one input {@link IMethodEnvironmentData} parameter
 * He should be marked as "public static" or have constructor with single {@link ODocument} input parameter
 * That constructor using just in case {@link IMethodEnvironmentData.getDisplayObjectModel}.getObject() instance of {@link ODocument}       
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ClassOMethod {
	/**
	 * For single call
	 * Using if displayed NOT in {@link MethodPlace}.DATA_TABLE
	 * @return
	 */
	public Class<? extends IMethod> methodClass() default OClassOMethod.class;
	/**
	 * For multiple calls
	 * Using if displayed in {@link MethodPlace}.DATA_TABLE   
	 * @return
	 */
	public Class<? extends IMethod> oClassTableMethodClass() default OClassTableOMethod.class;
	/**
	 * Just for {@link oClassTableMethodClass}
	 * @return
	 */
	public boolean resetSelection() default true;
	/**
	 * Real default title key generated by template "OClass.methodName"
	 * @return
	 */
	public String titleKey() default "";
	public FAIconType icon() default FAIconType.list;
	public BootstrapType bootstrap() default BootstrapType.DEFAULT;
	public boolean changingDisplayMode() default false;
	public boolean changingModel() default true;
	
	public int order() default 0;

	/**
	 * CREATE, READ, UPDATE, DELETE, EXECUTE
	 */
	public String permission() default ""; // hardcode link to PermissionFilter
	OFilter[] filters() default {};
	
	public Class<? extends Behavior>[] behaviors() default {};
	
}
