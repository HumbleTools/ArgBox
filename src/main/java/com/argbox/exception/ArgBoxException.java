package com.argbox.exception;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Exception checked permettant de lister un groupe d'erreurs.
 */
public class ArgBoxException extends Exception {

	private static final long serialVersionUID = -3672893259163891657L;

	/**
	 * La liste des messages d'erreurs, s'il y en a plusieurs.
	 */
	private List<String> errors;

	/**
	 * Inidique si l'exception sert à identifier plusieurs erreurs.
	 */
	private boolean multiple;

	/**
	 * Constructeur pour un unique message d'errur.
	 *
	 * @param message
	 *            Le message d'erreur unique.
	 */
	public ArgBoxException(final String message) {
		super(message);
		errors = new ArrayList<>();
		if (StringUtils.isNotBlank(message)) {
			errors.add(message);
		}
	}

	/**
	 * Constructeur pour plusieurs messages d'erreurs. Le premier paramètre est le
	 * message principal. Tous les messages seront cependant dans la liste des
	 * erreurs, le message principal en premier.
	 *
	 * @param message
	 *            Le message principal.
	 * @param errors
	 *            Les messages d'erreur secondaires.
	 */
	public ArgBoxException(final String message, final List<String> errors) {
		this(message);
		if (null != errors) {
			this.errors.addAll(errors);
			multiple = this.errors.size() > 1;
		}
	}

	/**
	 * Constructeur pour plusieurs messages d'erreurs. Sans message principal.
	 *
	 * @param errors
	 *            Les messages d'erreurs.
	 */
	public ArgBoxException(final List<String> errors) {
		this(null, errors);
	}

	/**
	 * @return the errors
	 */
	public List<String> getErrors() {
		return errors;
	}

	/**
	 * @param errors
	 *            the errors to set
	 */
	public void setErrors(final List<String> errors) {
		this.errors = errors;
	}

	/**
	 * @return the multiple
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * @param multiple
	 *            the multiple to set
	 */
	public void setMultiple(final boolean multiple) {
		this.multiple = multiple;
	}

}
