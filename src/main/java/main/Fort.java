package main;
import java.sql.Connection;

/**
 * Created by Zlodiak on 09.10.2016.
 */
public class Fort {
    String GUID="";
    int Lat=0;
    int Lng=0;
    int fortClass=1;
    int defenders=0;
    int attackers=0;
    Connection con;


    public Fort(String Guid, Connection CON) {
        GUID=Guid;
        con=CON;
    }

    public void Open() {
        //Вычитываем данные из базы по ГУИДу

    }

    public void Set(int LAT, int LNG, int FortClass) {
        //Записываем в базу новый форт и проверяем соединения с другими фортами

    }

    private void LinkTo(String Guid) {
        //Устанавливаем связь с указанным фортом, записываем ее в базу. Проверяем на создание замкнутой фигуры
    }

    private void checkClosing() {
        //Проверяем замкнутость
    }

    private void checkActivating() {
        //Проверяем возможность активации
    }

    public void Activate() {
        //Активируем, ищем все города внутри, каждому городу прописываем ГУИД поля (чтобы нельзя было повторно использовать)
    }

    private int countBonus() {
        //Считаем суммарный бонус городов внутри - нужно каждую отсечку пересчитывать
        return 0;
    }

    private void disableEnemyForts() {
        //Выключаем вражеские форты внутри замкнутой активированной фигуры - рушим линки и т.д.
    }

    private void destroyLinks(String Guid) {
        //уничтожить линки указанного форта
    }

    public void Attacked(int Attackers) {
        if (attackers+Attackers>=defenders)
            destroy();
        else attackers+=Attackers;
        update();
    }

    private void destroy() {
        //Уничтожаем форт, рушим все его линки, перепроверяем (уничтожаем) все фигуры, завязанные на него.
        //Надо как-то перепроврять и активировать альтернативные поля над городами, взамен уничтоженных
    }

    private void update() {
        //Апдейтим основные параметры в базе
    }


}
