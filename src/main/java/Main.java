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
                case 1 -> Functions.createCategory();
                case 2 -> Functions.deleteCategory();
                case 3 -> Functions.findTree();
                case 4 -> Functions.show();
                case 5 -> {
                    System.out.print("Вы точно хотите выйти? [y/n]: ");
                    String answer = scanner.nextLine();
                    if(answer.equals("n") || answer.equals("N")){
                        System.out.println("Продолжаем...");
                    } else if(answer.equals("y") || answer.equals("Y")){
                        System.out.println("Завершение работы...");
                        isWork = false;
                    }
                }
                default -> System.out.println("Введена неверная операция!");
            }
        }

    }

    private static void menu(){
        System.out.print("""
                Выберите операцию:\s
                1) Создать категорию
                2) Удалить категорию
                3) Найти категорию
                4) Вывести все данные
                5) Выход
                """);
    }
}
