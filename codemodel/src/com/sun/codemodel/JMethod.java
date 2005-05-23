/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.codemodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.codemodel.util.ClassNameComparator;

/**
 * Java method.
 */
public class JMethod extends JGenerifiableImpl implements JDeclaration, JAnnotatable {

	/**
	 * Modifiers for this method
	 */
	private JMods mods;

	/**
	 * Return type for this method
	 */
	private JType type = null;

	/**
	 * Name of this method
	 */
	private String name = null;

	/**
	 * List of parameters for this method's declaration
	 */
	private final List<JVar> params = new ArrayList<JVar>();

	/**
	 * Set of exceptions that this method may throw.
     * A set instance lazily created.
	 */
	private Set<JClass> _throws;

	/**
	 * JBlock of statements that makes up the body this method
	 */
	private JBlock body = null;

	private JDefinedClass outer;

	/**
	 * javadoc comments for this JMethod
	 */
	private JDocComment jdoc = null;

	/**
	 * Variable parameter for this method's varargs declaration
	 * introduced in J2SE 1.5
	 */
	private JVar varParam = null;

    /**
     * Annotations on this variable. Lazily created.
     */
    private List<JAnnotationUse> annotations = null;


	private boolean isConstructor() {
		return type == null;
	}
    
    /** To set the default value for the
     *  annotation member
     */
    private JExpression defaultValue = null;
    

	/**
	 * JMethod constructor
	 *
	 * @param mods
	 *        Modifiers for this method's declaration
	 *
	 * @param type
	 *        Return type for the method
	 *
	 * @param name
	 *        Name of this method
	 */
	JMethod(JDefinedClass outer, int mods, JType type, String name) {
		this.mods = JMods.forMethod(mods);
		this.type = type;
		this.name = name;
		this.outer = outer;
	}

	/**
	 * Constructor constructor
	 *
	 * @param mods
	 *        Modifiers for this constructor's declaration
	 *
	 * @param _class
	 *        JClass containing this constructor
	 */
	JMethod(int mods, JDefinedClass _class) {
		this.mods = JMods.forMethod(mods);
		this.type = null;
		this.name = _class.name();
		this.outer = _class;
	}
    
    private Set<JClass> getThrows() {
        if(_throws==null)
            _throws = new TreeSet<JClass>(ClassNameComparator.theInstance);
        return _throws;
    }

	/**
	 * Add an exception to the list of exceptions that this
	 * method may throw.
	 *
	 * @param exception
	 *        Name of an exception that this method may throw
	 */
	public JMethod _throws(JClass exception) {
        getThrows().add(exception);
		return this;
	}

	public JMethod _throws(Class exception) {
		return _throws(outer.owner().ref(exception));
	}

	/**
	 * Add the specified variable to the list of parameters
	 * for this method signature.
	 *
	 * @param type
	 *        JType of the parameter being added
	 *
	 * @param name
	 *        Name of the parameter being added
	 *
	 * @return New parameter variable
	 */
	public JVar param(int mods, JType type, String name) {
		JVar v = new JVar(JMods.forVar(mods), type, name, null);
		params.add(v);
		return v;
	}

	public JVar param(JType type, String name) {
		return param(JMod.NONE, type, name);
	}

	public JVar param(int mods, Class type, String name) {
		return param(mods, outer.owner()._ref(type), name);
	}

	public JVar param(Class type, String name) {
		return param(outer.owner()._ref(type), name);
	}

	/**
	 * @see #varParam(JType, String)
	 */
	public JVar varParam(Class type, String name) {
        return varParam(outer.owner()._ref(type),name);
    }

    /**
     * Add the specified variable argument to the list of parameters
     * for this method signature.
     *
     * @param type
     *      Type of the parameter being added.
     *
     * @param name
     *        Name of the parameter being added
     *
     * @return the variable parameter
     * 
     * @throws IllegalStateException
     *      If this method is called twice.
     *      varargs in J2SE 1.5 can appear only once in the 
     *      method signature.
     */
    public JVar varParam(JType type, String name) {
		if (!hasVarArgs()) {

            varParam =
				new JVar(
					JMods.forVar(JMod.NONE),
					type.array(),
					name,
					null);
			return varParam;
		} else {
			throw new IllegalStateException(
				"Cannot have two varargs in a method,\n"
					+ "Check if varParam method of JMethod is"
					+ " invoked more than once");

		}

	}

    /**
     * Adds an annotation to this variable.
     * @param clazz
     *          The annotation class to annotate the field with
     */
    public JAnnotationUse annotate(JClass clazz){
        if(annotations==null)
           annotations = new ArrayList<JAnnotationUse>();
        JAnnotationUse a = new JAnnotationUse(clazz);
        annotations.add(a);
        return a;
    }

    /**
     * Adds an annotation to this variable.
     *
     * @param clazz
     *          The annotation class to annotate the field with
     */
    public JAnnotationUse annotate(Class <? extends Annotation> clazz){
        return annotate(owner().ref(clazz));
    }

    public <W extends JAnnotationWriter> W annotate2(Class<W> clazz) {
        return TypedAnnotationWriter.create(clazz,this);
    }

	/**
	 * Check if there are any varargs declared
	 * for this method signature.
	 */
	public boolean hasVarArgs() {
		return this.varParam!=null;
	}

	public String name() {
		return name;
	}

	/**
	 * Returns the return type.
	 * @return
	 */
	public JType type() {
		return type;
	}

	/**
	 * Returns all the parameter types in an array.
	 * @return
	 *      If there's no parameter, an empty array will be returned.
	 */
	public JType[] listParamTypes() {
		JType[] r = new JType[params.size()];
		for (int i = 0; i < r.length; i++)
			r[i] = params.get(i).type();
		return r;
	}

	/**
	 * Returns  the varags parameter type.
	 * @return
	 * If there's no vararg parameter type, null will be returned.
	 */
	public JType listVarParamType() {
		if (varParam != null)
			return varParam.type();
		else
			return null;
	}

	/**
	 * Returns all the parameters in an array.
	 * @return
	 *      If there's no parameter, an empty array will be returned.
	 */
	public JVar[] listParams() {
		return params.toArray(new JVar[params.size()]);
	}

	/**
	 * Returns the variable parameter
	 * @return
	 *      If there's no parameter, null will be returned.
	 */
	public JVar listVarParam() {
		return varParam;
	}

	/**
	 * Returns true if the method has the specified signature.
	 */
	public boolean hasSignature(JType[] argTypes) {
		JVar[] p = listParams();
		if (p.length != argTypes.length)
			return false;

		for (int i = 0; i < p.length; i++)
			if (!p[i].type.equals(argTypes[i]))
				return false;

		return true;
	}

	/**
	 * Get the block that makes up body of this method
	 *
	 * @return Body of method
	 */
	public JBlock body() {
		if (body == null)
			body = new JBlock();
		return body;
	}
    
    /**
     * Specify the default value for this annotation member
     * @param value 
     *           Default value for the annotation member
     * 
     */
    public void declareDefaultValue(JExpression value){
        this.defaultValue = value;
    }

	/**
	 * Creates, if necessary, and returns the class javadoc for this
	 * JDefinedClass
	 *
	 * @return JDocComment containing javadocs for this class
	 */
	public JDocComment javadoc() {
		if (jdoc == null)
			jdoc = new JDocComment(owner());
		return jdoc;
	}

	public void declare(JFormatter f) {
		if (jdoc != null)
			f.g(jdoc);

        if (annotations != null){
            for (JAnnotationUse a : annotations)
                f.g(a).nl();
        }

		// declare the generics parameters
		super.declare(f);

		f.g(mods);
		if (!isConstructor())
			f.g(type);
		f.id(name).p('(');
		boolean first = true;
        for (JVar var : params) {
            if (!first)
                f.p(',');
            f.b(var);
            first = false;
        }
		if (hasVarArgs()) {
			if (!first)
				f.p(',');
			f.g(varParam.type);
			f.p("... ");
			f.id(varParam.name());
		}

		f.p(')');
		if (_throws!=null && !_throws.isEmpty()) {
			f.nl().i().p("throws").g(_throws).nl().o();
		}
        
        if (defaultValue != null) {
            f.p("default ");
            f.g(defaultValue);
        }
		if (body != null) {
			f.s(body);
		} else if (
			!outer.isInterface() && !outer.isAnnotationTypeDeclaration() && !mods.isAbstract() && !mods.isNative()) {
			// Print an empty body for non-native, non-abstract methods
			f.s(new JBlock());
		} else {
			f.p(';').nl();
		}
	}

	/**
	 * @return
	 *      the current modifiers of this method.
	 *      Always return non-null valid object. 
	 */
	public JMods getMods() {
		return mods;
	}

	protected JCodeModel owner() {
		return outer.owner();
	}
}
