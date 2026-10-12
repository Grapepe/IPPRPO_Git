package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RentalService
{
    private static final Logger LOGGER = Logger.getLogger(RentalService.class.getName());

    private List<Rent> rentList;
    private List<User> userList;
    private List<RentalOffice> officeList;

    private static final String DATA_FILE = "rental_data.ser";

    public RentalService()
    {
        this.rentList = new ArrayList<>();
        this.userList = new ArrayList<>();
        this.officeList = new ArrayList<>();
        setupLogger();
        loadData();
    }


    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("rental_system.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.INFO);
            LOGGER.info("Система проката запущена. Логирование инициализировано.");
        } catch (IOException e) {
            System.err.println("Ошибка настройки логгера: " + e.getMessage());
        }
    }
    public boolean isLoginTaken(String login) {
        if (login == null) return false;
        for (User u : userList) {
            if (u.getLogin() != null && u.getLogin().equalsIgnoreCase(login)) return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    public void loadData()
    {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            LOGGER.info("Файл данных не найден. Создана новая система.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.userList = (List<User>) ois.readObject();
            this.officeList = (List<RentalOffice>) ois.readObject();
            this.rentList = (List<Rent>) ois.readObject();
            LOGGER.info("Данные успешно загружены из файла.");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Ошибка загрузки данных", e);
        }
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(userList);
            oos.writeObject(officeList);
            oos.writeObject(rentList);
            LOGGER.info("Данные успешно сохранены в файл.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Ошибка сохранения данных", e);
        }
    }

    public boolean registerClient(String login, String password, String email,
                                  String license, String phone) {
        if (isLoginTaken(login)) {
            System.out.println("Логин \"" + login + "\" уже занят.");
            return false;
        }
        Client client = new Client(login, password, email, license, phone);
        userList.add(client);
        LOGGER.info("Зарегистрирован новый клиент: " + login);
        saveData();
        return true;
    }

    public boolean registerManager(String login, String password, String email,
                                   long emplId, RentalOffice office) {
        if (isLoginTaken(login)) {
            System.out.println("Логин \"" + login + "\" уже занят.");
            return false;
        }
        Manager m = new Manager(login, password, email, emplId);
        m.setRentalOffice(office);
        userList.add(m);
        LOGGER.info("Зарегистрирован менеджер: " + login
                + " (офис: " + (office != null ? office.getName() : "—") + ")");
        saveData();
        return true;
    }

    public User authenticate(String login, String password) {
        for (User user : userList) {
            if (user.authenticate(login, password)) {
                LOGGER.info("Пользователь " + login + " вошел в систему.");
                return user;
            }
        }
        LOGGER.warning("Неудачная попытка входа: " + login);
        return null;
    }

    public void createOffice(String name, String address, String phone) {
        RentalOffice office = new RentalOffice(name, address, phone);
        officeList.add(office);
        LOGGER.info("Создан новый пункт проката: " + name);
        saveData();
    }

    public boolean addCarToOffice(RentalOffice office, Car car)
    {
        if (office == null || car == null) return false;
        if (office.findCarByVin(car.getVin()) != null) {
            System.out.println("Машина с таким VIN уже есть в офисе.");
            return false;
        }
        office.addCar(car);
        LOGGER.info("Добавлен автомобиль " + car.getMark() + " (" + car.getVin()
                + ") в офис " + office.getName());
        saveData();
        return true;
    }

    public boolean removeCarFromOffice(RentalOffice office, String vin) {
        if (office == null) return false;
        Car car = office.findCarByVin(vin);
        if (car == null) {
            System.out.println("Машина с VIN " + vin + " не найдена в офисе.");
            return false;
        }
        if (car.getRentStatus() == RentStatus.RENTED) {
            System.out.println("Нельзя удалить машину, которая сейчас в аренде.");
            return false;
        }
        office.removeCar(car);
        LOGGER.info("Удалён автомобиль " + car.getVin() + " из офиса " + office.getName());
        saveData();
        return true;
    }
    public void processRental(Client client, Car car, RentalOffice office, Insurance insurance, java.time.LocalDate start, java.time.LocalDate end) {
        if (!car.isAvailable()) {
            LOGGER.warning("Попытка аренды недоступного автомобиля: " + car.getVin());
            System.out.println("Автомобиль недоступен!");
            return;
        }

        double cost = car.calculatePrice((int) java.time.temporal.ChronoUnit.DAYS.between(start, end));
        if (insurance != null) cost += insurance.calculateCost((int) java.time.temporal.ChronoUnit.DAYS.between(start, end));

        Payment payment = new Payment(cost, "CARD");
        payment.processPayment();

        if (payment.getStatus().equals("SUCCESS")) {
            Rent rent = new Rent(client, car, office, insurance, start, end);
            car.setStatus(RentStatus.RENTED);
            rentList.add(rent);
            LOGGER.info("Оформлена аренда: " + rent.getRentId() + " для " + client.getLogin());
            System.out.println("Аренда успешно оформлена! Итоговая стоимость: " + cost);
            saveData();
        } else {
            LOGGER.warning("Ошибка оплаты для клиента: " + client.getLogin());
            System.out.println("Ошибка оплаты. Аренда не оформлена.");
        }
    }

    public void returnCar(Rent rent)
    {
        if (rent.getStatus() == RentStatus.RENTED)
        {
            rent.completeRent();
            LOGGER.info("Автомобиль возвращен. Аренда " + rent.getCar().getVin() + " завершена.");
            System.out.println("Автомобиль успешно возвращен!");
            saveData();
        } else
        {
            System.out.println("Эта аренда не активна.");
        }
    }

    public boolean updateCar(RentalOffice office, String vin,
                             String newMark, String newModel,
                             CarCategory newCategory, double newPrice) {
        Car car = office.findCarByVin(vin);
        if (car == null) {
            System.out.println("Машина не найдена.");
            return false;
        }
        if (car.getRentStatus() == RentStatus.RENTED) {
            System.out.println("Нельзя редактировать машину, которая в аренде.");
            return false;
        }
        if (newMark != null && !newMark.isEmpty())       car.setMark(newMark);
        if (newModel != null && !newModel.isEmpty())     car.setModel(newModel);
        if (newCategory != null)                         car.setCarCategory(newCategory);
        if (newPrice > 0)                                car.setBaseDailyPrice(newPrice);

        LOGGER.info("Обновлена машина " + vin + " в офисе " + office.getName());
        saveData();
        return true;
    }
    public List<RentalOffice> getOfficeList() { return officeList; }
    public List<User> getUserList() { return userList; }
    public List<Rent> getRentList() { return rentList; }
}