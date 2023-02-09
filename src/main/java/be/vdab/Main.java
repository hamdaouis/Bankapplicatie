package be.vdab;

import be.vdab.domain.Rekening;
import be.vdab.exceptions.*;
import be.vdab.repositories.RekeningRepository;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Maak je keuze: ");
        System.out.println("1. Nieuwe rekening");
        System.out.println("2. Saldo consulteren");
        System.out.println("3. Overschrijven");
        var scanner = new Scanner(System.in);
        var keuze = scanner.nextInt();
        scanner.nextLine();
        var repository = new RekeningRepository();
        switch (keuze) {
            case 1 -> {
                System.out.println("Rekeningnummer: ");
                try {
                    var rekeningnummer = new Rekening(scanner.nextLine());
                    try {
                        repository.nieuwRekeningnummer(rekeningnummer);
                    } catch (SQLException ex) {
                        System.out.println("Rekening bestaat al");
                    }

                } catch (OngeldigRekeningnummerException ex) {
                    System.out.println("Ongeldig rekeningnummer");
                }
            }
            case 2 -> {
                System.out.println("Rekeningnummer: ");
                try {
                    var rekeningnummer = new Rekening(scanner.nextLine());
                    try {
                        repository.getSaldo(rekeningnummer).ifPresentOrElse(
                                saldo -> System.out.println("Saldo: " + saldo),
                                () -> System.out.println("Rekeningnummer niet gevonden"));
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } catch (OngeldigRekeningnummerException ex) {
                    System.out.println("Ongeldig rekeningnummer");
                }
            }
            case 3 -> {
                System.out.println("Van rekeningnummer: ");
                try {
                    var vanRekeningnummer = new Rekening(scanner.nextLine());
                    System.out.println("Naar rekeningnummer: ");
                    var naarRekeningnummer = new Rekening(scanner.nextLine());
                    System.out.println("Bedrag: ");
                    var bedrag = scanner.nextBigDecimal();
                    try {
                        repository.overschrijven(vanRekeningnummer, naarRekeningnummer, bedrag);
                        System.out.println(bedrag + " EUR is overgeschreven van rekeningnummer " + vanRekeningnummer.getRekeningnummer() + " naar rekeningnummer " + naarRekeningnummer.getRekeningnummer());
                    } catch (BedragTeLaagException ex) {
                        System.out.println("Bedrag te laag");
                    } catch (SaldoOntoereikendException ex) {
                        System.out.println("Saldo ontoereikend");
                    } catch (ZelfdeRekeningnummerException ex) {
                        System.out.println("De opgegeven rekeningnummers zijn hetzelfde");
                    } catch (RekeningBestaatNietException ex) {
                        System.out.println("Rekeningnummer bestaat niet");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } catch (OngeldigRekeningnummerException ex) {
                    System.out.println("Ongeldig rekeningnummer");
                }
            }
        }
    }
}
