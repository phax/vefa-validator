package no.difi.vefa.validator.properties;

import no.difi.vefa.validator.api.IProperties;

abstract class AbstractProperties implements IProperties {

    @Override
    public Object get(String key) {
        return get(key, null);
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

}
