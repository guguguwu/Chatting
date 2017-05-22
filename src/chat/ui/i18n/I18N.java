package chat.ui.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class I18N {
    
    private static ResourceBundle bundle;
    
    public static String getString(String key) {
        return getBundle().getString(key);
    }
    
    public static String getString(String key, Object... arguments) {
        final String pattern = getString(key);
        return MessageFormat.format(pattern, arguments);
    }
    
    public static synchronized ResourceBundle getBundle() {
        if (bundle == null) {
            final String packageName = I18N.class.getPackage().getName();
            bundle = ResourceBundle.getBundle(packageName + ".ChatApp"); //NOI18N
        }
        
        return bundle;
    }

	public static void main(String[] args) {
		System.out.println(I18N.getBundle().getLocale());
		System.out.println(I18N.getString("ui.main.btn"));
		System.out.println(I18N.getString("ui.main.btn2", "param1"));
	}
}
