package be.vdab.domain;

import be.vdab.exceptions.OngeldigRekeningnummerException;

import java.math.BigDecimal;

public class Rekening {
    private final String rekeningnummer;
    private BigDecimal saldo = BigDecimal.ZERO;

    public Rekening(String rekeningnummer) {
        if (valideerRekeningnummer(rekeningnummer)) {
            this.rekeningnummer = rekeningnummer;
        } else {
            throw new OngeldigRekeningnummerException();
        }
    }
    public String getRekeningnummer() {
        return rekeningnummer;
    }

    public boolean valideerRekeningnummer(String rekeningnummer) {
        if (rekeningnummer.length() != 16 || !(rekeningnummer.substring(0, 2).equalsIgnoreCase("be"))) {
            throw new OngeldigRekeningnummerException();
        }
        var controlegetal = Integer.parseInt(rekeningnummer.substring(2, 4));
        if (controlegetal < 2 || controlegetal > 98) {
            throw new OngeldigRekeningnummerException();
        }
        var cijfersVanafVijfdePositie = rekeningnummer.substring(4);
        var volledigControlegetal = cijfersVanafVijfdePositie + "1114" + controlegetal;
        var result = Long.parseLong(volledigControlegetal) % 97 == 1;
        if (!result) {
            throw new OngeldigRekeningnummerException();
        }
        return true;
    }
}
