package de.projektlabor.makiekillerbot.cac.config.loading.loaders;

import de.projektlabor.makiekillerbot.cac.config.loading.Loader;

public class IntLoader implements Loader<Integer>{

	@Override
	public String saveValue(Integer obj) {
		return obj.toString();
	}

	@Override
	public Integer loadValue(String value) throws Exception{
		return Integer.valueOf(value);
	}

}
