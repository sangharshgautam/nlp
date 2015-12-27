package uk.co.sangharsh.nlp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class ObjectUtil {

	public static <T> T loadObjectNoExceptions(String path, Class<T> clazz) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File(path)));
			@SuppressWarnings("unchecked")
			T readObject = (T) ois.readObject();
			ois.close();
			return readObject;
		} catch (Exception e) {
			return null;
		}
	}

}
