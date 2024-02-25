import jakarta.persistence.*;
import Entity.*;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean isWork = true;
        Scanner scanner = new Scanner(System.in);
        while(isWork){
            menu();
            int operation = Integer.parseInt(scanner.nextLine());
            switch(operation){
                case 1 -> Functions .createCategory();
                case 2 -> Functions.deleteCategory();
                case 3 -> Functions.findTree();
                case 4 -> Functions.show();
                case 5 -> Functions.relocateCategory();
                case 6 -> isWork = exit();
                default -> System.out.println("Введена неверная операция!");
            }
        }

    }

    private static boolean exit(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Вы точно хотите выйти? [y/n]: ");
        String answer = scanner.nextLine();
        if(answer.equals("n") || answer.equals("N")){
            System.out.println("Продолжаем...");
            return true;
        } else if(answer.equals("y") || answer.equals("Y")){
            System.out.println("Завершение работы...");
            return false;
        }
        return true;
    }
    private static void menu(){
        System.out.print("""
                Выберите операцию:\s
                1) Создать категорию
                2) Удалить категорию
                3) Найти категорию
                4) Вывести все данные
                5) Переместить категорию
                6) Выход
                """);
    }
}
