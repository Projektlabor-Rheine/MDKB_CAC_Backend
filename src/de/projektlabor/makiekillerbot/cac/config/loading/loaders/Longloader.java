package de.projektlabor.makiekillerbot.cac.config.loading.loaders;

import de.projektlabor.makiekillerbot.cac.config.loading.Loader;

public class Longloader implements Loader<Long>{

	@Override
	public String saveValue(Long obj) {
		return obj.toString();
	}

	@Override
	public Long loadValue(String value) throws Exception{
		return Long.valueOf(value);
	}

}
