package customapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Allows for dynamic manual reloading of classes, i.e. CustomFunctions
 *
 */
public class MyClassLoader extends ClassLoader{

    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(!"customapi.CustomFunctions".equals(name))
                return super.loadClass(name);

        
        try {
            String url = "./bin/customapi/CustomFunctions.class";
            URL myUrl = new URL(new URL("file:"), url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass("customapi.CustomFunctions",
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}