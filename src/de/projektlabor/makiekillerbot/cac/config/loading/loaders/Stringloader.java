package de.projektlabor.makiekillerbot.cac.config.loading.loaders;

import de.projektlabor.makiekillerbot.cac.config.loading.Loader;

public class Stringloader implements Loader<String>{

	@Override
	public String saveValue(String obj) {
		return obj;
	}

	@Override
	public String loadValue(String value) throws Exception {
		return value;
	}

}
