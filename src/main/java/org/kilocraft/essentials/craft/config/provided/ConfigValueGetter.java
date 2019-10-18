package org.kilocraft.essentials.craft.config.provided;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.kilocraft.essentials.craft.config.provided.localVariables.LocalConfigVariable;

public class ConfigValueGetter {
    private FileConfig config;
    private ProvidedValueReplaced valueReplaced;
    private LocalVariableReplaced localReplaced;

    public ConfigValueGetter(FileConfig fileConfig) {
        this.config = fileConfig;
        this.valueReplaced = new ProvidedValueReplaced(this.config);
        localReplaced = new LocalVariableReplaced();
    }

    public String get(boolean allowGlobalObjects, String key) {
        return valueFormatter(allowGlobalObjects, key);
    }

    public String getFormatter(boolean allowGlobalObjects, String key, Object... objects) {
        return customValueFormatter(allowGlobalObjects, key, objects);
    }

    public <L extends LocalConfigVariable> String getLocal(boolean replaceLocal, String key, L localFormatter) {
        return replaceLocal ? valueWithLocalFormat(key, localFormatter) : this.config.getOrElse(key, "NULL");
    }

    public <L extends LocalConfigVariable> String getLocalFormatter(boolean replaceLocal, String key, L localFormatter, Object... objects) {
        String string = replaceLocal ? valueWithLocalFormat(key, localFormatter) : this.config.getOrElse(key, "NULL");
        return String.format(string, objects);
    }

    public <T> T getValue(String key) {
        return this.config.get(key);
    }

    public ProvidedValueReplaced getValueReplacer() {
        return valueReplaced;
    }

    public LocalVariableReplaced getLocalReplacer() {
        return localReplaced;
    }

    private String valueFormatter(boolean allowGlobalObjects, String key) {
        String value = this.config.get(key);
        return allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(value) : value;
    }

    private String customValueFormatter(boolean allowGlobalObjects, String key, Object... objects) {
        String value = this.config.getOrElse(key, "NULL");
        String result = allowGlobalObjects ? this.valueReplaced.replaceGlobalObjects(value) : value;
        return String.format(result, objects);
    }

    private <L extends LocalConfigVariable> String valueWithLocalFormat(String key, L formatter) {
        return localReplaced.replace(this.config.getOrElse(key, "NULL"), formatter);
    }


}