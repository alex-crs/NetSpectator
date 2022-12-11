package services;

import entities.Device;
import handlers.MainHandler;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.transaction.Transactional;

@Transactional
public class dataBaseService {
    private static final Logger LOGGER = Logger.getLogger(dataBaseService.class);

    SessionFactory factory = new Configuration() //удалить
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(Device.class)
            .buildSessionFactory();
    Session session = null;

    //возвращает полный список устройств со статусами
    public static String[] getDeiceList() {

        return null;
    }

    //возвращает список устройств со статусами по определенной группе
    public static String[] getDeviceListByGroup(String groupName) {

        return null;
    }

    //добавляет новое устройство в базу данных
    public static int addDevice(Device device){

        return -1;
    }

    //удаляет выбранное устройство из базы данных
    public static int deleteDevice(Device device){

        return -1;
    }

    //изменяет статус устройства (мне кажется этот метод не понадобиться
    //так как после подключения hibernate наблюдает за объектами
    public static int changeDeviceStatus(Device device){

        return -1;
    }

}
