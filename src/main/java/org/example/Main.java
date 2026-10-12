package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main
{
    private static RentalService service = new RentalService();
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args)
    {
        while (true)
        {
            System.out.println("Hello, world");
            System.out.println("\nСИСТЕМА ПРОКАТА АВТОМОБИЛЕЙ\n1. Войти\n2. Зарегистрироваться\n3. Выйти");
            int choice = readInt("Выберите действие: ");
            switch (choice)
            {
                case 1: login(); break;
                case 2: register(); break;
                case 3:
                    service.saveData();
                    System.out.println("Выход из системы...");
                    return;
                default:
                    System.out.println("Неверный ввод. Попробуйте снова.");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true)
        {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try
            {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число (например, 1, 2, 3).");
            }
        }
    }

    private static LocalDate readDate(String prompt)
    {
        while (true) {
            System.out.print(prompt + " (дд.ММ.гггг): ");
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: неверный формат даты. Пример правильного ввода: 20.09.2026");
            }
        }
    }

    private static void register() {
        int role = readInt("Кто регистрируется? (1 - клиент, 2 - менеджер): ");

        System.out.print("Логин: ");   String login    = scanner.nextLine().trim();
        System.out.print("Пароль: ");  String password = scanner.nextLine().trim();
        System.out.print("Email: ");   String email    = scanner.nextLine().trim();

        if (login.isEmpty() || password.isEmpty()) {
            System.out.println("Логин и пароль обязательны.");
            return;
        }

        if (role == 1) {
            System.out.print("Вод. удостоверение: "); String lic   = scanner.nextLine().trim();
            System.out.print("Телефон: "); String phone = scanner.nextLine().trim();
            if (service.registerClient(login, password, email, lic, phone)) {
                System.out.println("Клиент зарегистрирован.");
            }
        } else if (role == 2) {
            RentalOffice office = selectOffice();
            if (office == null) return;
            long emplId = System.currentTimeMillis();
            if (service.registerManager(login, password, email, emplId, office)) {
                System.out.println("Менеджер зарегистрирован в офисе " + office.getName());
            }
        } else {
            System.out.println("Неизвестная роль.");
        }
    }

    private static void login() {
        System.out.print("Логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        User user = service.authenticate(login, password);
        if (user == null) {
            System.out.println("Неверный логин или пароль.");
            return;
        }

        if (user instanceof Client) {
            clientMenu((Client) user);
        } else if (user instanceof Manager) {
            managerMenu((Manager) user);
        }
    }

    private static void clientMenu(Client client) {
        RentalOffice currentOffice = null;

        while (true) {
            System.out.println("\n--- МЕНЮ КЛИЕНТА ---");
            System.out.println("Текущий офис: " + (currentOffice == null ? "не выбран" : currentOffice.getName()));
            System.out.println("1. Выбрать офис");
            System.out.println("2. Показать доступные авто офиса");
            System.out.println("3. Арендовать автомобиль");
            System.out.println("4. Моя история аренд");
            System.out.println("5. Назад");

            int choice = readInt("Выбор: ");
            switch (choice) {
                case 1: currentOffice = selectOffice(); break;
                case 2:
                    if (currentOffice == null) { System.out.println("Сначала выберите офис."); break; }
                    showAvailableCars(currentOffice);
                    break;
                case 3:
                    if (currentOffice == null) { System.out.println("Сначала выберите офис."); break; }
                    rentCar(client, currentOffice);
                    break;
                case 4: showHistory(client); break;
                case 5: return;
                default: System.out.println("Неверный пункт.");
            }
        }
    }

    private static RentalOffice selectOffice() {
        List<RentalOffice> offices = service.getOfficeList();
        if (offices.isEmpty()) {
            System.out.println("Офисов нет.");
            return null;
        }
        for (int i = 0; i < offices.size(); i++) {
            System.out.println((i + 1) + ". " + offices.get(i).getName());
        }
        int idx = readInt("Номер офиса: ") - 1;
        if (idx < 0 || idx >= offices.size()) {
            System.out.println("Неверный номер.");
            return null;
        }
        return offices.get(idx);
    }

    private static void showAvailableCars(RentalOffice office) {
        List<Car> available = office.getAvailableCars();
        if (available.isEmpty()) {
            System.out.println("В офисе нет доступных авто.");
            return;
        }
        System.out.println("Офис: " + office.getName());
        for (Car car : available) System.out.println("  -> " + car);
    }

    private static void rentCar(Client client, RentalOffice office) {
        System.out.print("Введите VIN автомобиля: ");
        String vin = scanner.nextLine().trim();

        Car selectedCar = null;
        for (Car car : office.getCarList()) {
            if (car.getVin().equalsIgnoreCase(vin) && car.isAvailable()) {
                selectedCar = car;
                break;
            }
        }
        if (selectedCar == null) {
            System.out.println("Автомобиль не найден или недоступен в этом офисе.");
            return;
        }

        LocalDate start = LocalDate.now();
        System.out.println("Дата начала аренды: " + start);

        LocalDate end;
        while (true) {
            end = readDate("Введите дату окончания аренды");
            if (!end.isAfter(start)) {
                System.out.println("Дата окончания должна быть позже " + start);
            } else break;
        }

        Insurance insurance = null;
        int ins = readInt("Нужна страховка? (1 - Да, 0 - Нет): ");
        if (ins == 1) {
            insurance = new Insurance("Полная", 10000, 10.0);
            System.out.println("Страховка 'Полная' добавлена (10$/сутки).");
        }

        service.processRental(client, selectedCar, office, insurance, start, end);
    }

    private static void showHistory(Client client)
    {
        System.out.println("\n--- ВАША ИСТОРИЯ АРЕНД ---");
        boolean found = false;
        for (Rent rent : service.getRentList()) {
            if (rent.getClient() != null
                    && rent.getClient().getLogin().equals(client.getLogin())) {
                System.out.println(rent);
                found = true;
            }
        }
        if (!found) {
            System.out.println("У вас ещё нет аренд.");
        }
    }

    private static void managerMenu(Manager manager) {
        RentalOffice office = manager.getRentalOffice();
        if (office == null) {
            System.out.println("Вам не назначен офис.");
            return;
        }
        while (true) {
            System.out.println("\n--- МЕНЮ МЕНЕДЖЕРА (" + office.getName() + ") ---");
            System.out.println("1. Машины офиса");
            System.out.println("2. Активные аренды офиса");
            System.out.println("3. Принять автомобиль (завершить аренду)");
            System.out.println("4. Найти аренды клиента");
            System.out.println("5. Добавить автомобиль");
            System.out.println("6. Редактировать автомобиль");
            System.out.println("7. Удалить автомобиль");
            System.out.println("8. Назад");

            int c = readInt("Выбор: ");
            switch (c) {
                case 1: showOfficeCars(office);      break;
                case 2: showOfficeRents(office);     break;
                case 3: returnCar(office);           break;
                case 4: findClientRents(office);     break;
                case 5: addCarInteractive(office);   break;
                case 6: editCarInteractive(office);  break;
                case 7: deleteCarInteractive(office);break;
                case 8: return;
                default: System.out.println("Неверный пункт.");
            }
        }
    }
    private static void findClientRents(RentalOffice office)
    {
        System.out.print("Логин клиента: ");
        String login = scanner.nextLine().trim();
        boolean found = false;
        System.out.println("\n--- АРЕНДЫ КЛИЕНТА \"" + login + "\" В ОФИСЕ \"" + office.getName() + "\" ---");
        for (Rent r : service.getRentList()) {
            if (r.getClient() == null) continue;
            if (!r.getClient().getLogin().equals(login)) continue;
            if (r.getRentalOffice() == null
                    || !r.getRentalOffice().getName().equals(office.getName())) continue;
            System.out.println(r);
            found = true;
        }
        if (!found) System.out.println("Ничего не найдено.");
    }

    private static void addCarInteractive(RentalOffice office) {
        System.out.println("\n--- ДОБАВЛЕНИЕ АВТОМОБИЛЯ В \"" + office.getName() + "\" ---");

        System.out.print("VIN: ");
        String vin = scanner.nextLine().trim();
        if (vin.isEmpty()) { System.out.println("VIN обязателен."); return; }

        if (office.findCarByVin(vin) != null) {
            System.out.println("Машина с таким VIN уже есть в офисе.");
            return;
        }

        System.out.print("Марка: ");
        String mark = scanner.nextLine().trim();
        System.out.print("Модель: ");
        String model = scanner.nextLine().trim();

        CarCategory category = readCategory();
        if (category == null) return;

        double price = readDouble("Базовая цена за сутки: ");
        if (price <= 0) { System.out.println("Цена должна быть больше 0."); return; }

        Car car = new Car(vin, mark, model, category, price);
        if (service.addCarToOffice(office, car)) {
            System.out.println("Автомобиль " + mark + " " + model + " добавлен.");
        }
    }
    private static void editCarInteractive(RentalOffice office) {
        System.out.println("\n--- РЕДАКТИРОВАНИЕ АВТОМОБИЛЯ ---");

        System.out.print("VIN редактируемой машины: ");
        String vin = scanner.nextLine().trim();

        Car car = office.findCarByVin(vin);
        if (car == null) { System.out.println("Машина не найдена."); return; }
        if (car.getRentStatus() == RentStatus.RENTED) {
            System.out.println("Машина сейчас в аренде — редактирование запрещено.");
            return;
        }

        System.out.println("Текущие данные: " + car);
        System.out.println("Оставьте поле пустым, чтобы не менять.");

        System.out.print("Новая марка [" + car.getMark() + "]: ");
        String mark = scanner.nextLine().trim();

        System.out.print("Новая модель [" + car.getModel() + "]: ");
        String model = scanner.nextLine().trim();

        System.out.println("Класс (1-ECONOM, 2-BUSINESS, Enter — оставить [" + car.getCarCategory() + "]): ");
        CarCategory category = null;
        String catInput = scanner.nextLine().trim();
        if (catInput.equals("1")) category = CarCategory.ECONOM;
        else if (catInput.equals("2")) category = CarCategory.BUSINESS;

        System.out.print("Новая цена [" + car.getBaseDailyPrice() + "]: ");
        String priceStr = scanner.nextLine().trim();
        double price = -1;
        if (!priceStr.isEmpty()) {
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) {
                System.out.println("Некорректная цена — оставляем старую.");
            }
        }

        service.updateCar(office, vin, mark, model, category, price);
        System.out.println("Обновлённая запись: " + car);
    }

    private static void deleteCarInteractive(RentalOffice office) {
        System.out.println("\n--- УДАЛЕНИЕ АВТОМОБИЛЯ ---");
        System.out.print("VIN: ");
        String vin = scanner.nextLine().trim();

        Car car = office.findCarByVin(vin);
        if (car == null) { System.out.println("Машина не найдена."); return; }

        System.out.println("Вы собираетесь удалить: " + car);
        int confirm = readInt("Подтвердить удаление? (1 - Да, 0 - Нет): ");
        if (confirm != 1) {
            System.out.println("Отменено.");
            return;
        }

        if (service.removeCarFromOffice(office, vin)) {
            System.out.println("Автомобиль удалён.");
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число (например, 25.5).");
            }
        }
    }

    private static CarCategory readCategory() {
        int c = readInt("Класс (1 - ECONOM, 2 - BUSINESS): ");
        if (c == 1) return CarCategory.ECONOM;
        if (c == 2) return CarCategory.BUSINESS;
        System.out.println("Неверный класс.");
        return null;
    }

    private static void showOfficeRents(RentalOffice office) {
        boolean any = false;
        for (Rent r : service.getRentList()) {
            if (r.getStatus() == RentStatus.RENTED
                    && r.getCar() != null
                    && office.getCarList().contains(r.getCar())) {
                System.out.println(r);
                any = true;
            }
        }
        if (!any) System.out.println("Активных аренд нет.");
    }

    private static void showOfficeCars(RentalOffice office) {
        System.out.println("\n--- МАШИНЫ ОФИСА \"" + office.getName() + "\" ---");
        List<Car> cars = office.getCarList();
        if (cars.isEmpty()) {
            System.out.println("Пусто.");
            return;
        }
        System.out.printf("%-10s %-12s %-12s %-10s %-12s %-12s%n",
                "VIN", "Марка", "Модель", "Класс", "Цена/сутки", "Статус");
        System.out.println("--------------------------------------------------------------------------");
        for (Car car : cars) {
            System.out.printf("%-10s %-12s %-12s %-10s %-12.2f %-12s%n",
                    car.getVin(),
                    car.getMark(),
                    car.getModel(),
                    car.getCarCategory(),
                    car.getBaseDailyPrice(),
                    car.getRentStatus());
        }
    }

    private static void returnCar(RentalOffice office)
    {
        System.out.print("VIN: ");
        String vin = scanner.nextLine().trim();
        for (Rent r : service.getRentList()) {
            if (r.getStatus() == RentStatus.RENTED
                    && r.getCar().getVin().equalsIgnoreCase(vin)
                    && office.getCarList().contains(r.getCar())) {
                service.returnCar(r);
                return;
            }
        }
        System.out.println("Активная аренда не найдена.");
    }
}