package be.vdab.repositories;

import be.vdab.domain.Rekening;
import be.vdab.exceptions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class RekeningRepository extends AbstractRepository {
    public void overschrijven(Rekening vanRekeningnummer, Rekening naarRekeningnummer, BigDecimal bedrag) throws SQLException {
        var sqlVanRekening = """
                select saldo
                from rekeningen
                where nummer = ?
                for update
                """;
        var sqlNaarRekening = """
                select saldo
                from rekeningen
                where nummer = ?
                for update
                """;
        try (var connection = super.getConnection();
             var vanStatement = connection.prepareStatement(sqlVanRekening);
        var naarStatement = connection.prepareStatement(sqlNaarRekening)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            vanStatement.setString(1, vanRekeningnummer.getRekeningnummer());
            naarStatement.setString(1, naarRekeningnummer.getRekeningnummer());
            if (bedrag.compareTo(BigDecimal.ZERO) <= 0) {
                connection.rollback();
                throw new BedragTeLaagException();
            }
            if (vanRekeningnummer.equals(naarRekeningnummer)) {
                connection.rollback();
                throw new ZelfdeRekeningnummerException();
            }
            naarStatement.executeQuery().next();
            var result = vanStatement.executeQuery();
            if (!result.next()) {
                connection.rollback();
                throw new RekeningBestaatNietException();
            }
            var saldo = result.getBigDecimal("saldo");

            if (saldo.compareTo(bedrag) < 0) {
                connection.rollback();
                throw new SaldoOntoereikendException();
            }
            verlaagSaldo(vanRekeningnummer, bedrag, connection);
            verhoogSaldo(naarRekeningnummer, bedrag, connection);
            connection.commit();
        }
    }

    public void verhoogSaldo(Rekening rekeningnummer, BigDecimal bedrag, Connection connection) throws SQLException {
        var sql = """
                update rekeningen
                set saldo = saldo + ?
                where nummer = ?
                """;
        try (var statement = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setBigDecimal(1, bedrag);
            statement.setString(2, rekeningnummer.getRekeningnummer());
            var result = statement.executeUpdate();
            if (result == 0) {
                connection.rollback();
                throw new RekeningBestaatNietException();
            }
        }
    }

    public void verlaagSaldo(Rekening rekeningnummer, BigDecimal bedrag, Connection connection) throws SQLException {
        var sql = """
                update rekeningen
                set saldo = saldo - ?
                where nummer = ?
                """;
        try (var statement = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setBigDecimal(1, bedrag);
            statement.setString(2, rekeningnummer.getRekeningnummer());
            statement.executeUpdate();
        }
    }

    public Optional<BigDecimal> getSaldo(Rekening rekeningnummer) throws SQLException {
        var sql = """
                select saldo
                from rekeningen
                where nummer = ?
                """;
        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setString(1, rekeningnummer.getRekeningnummer());
            var result = statement.executeQuery();
            return result.next() ? Optional.of(result.getBigDecimal("saldo")) : Optional.empty();
        }
    }

    public void nieuwRekeningnummer(Rekening rekeningnummer) throws SQLException {
        var sql = """
                INSERT INTO rekeningen (nummer)
                VALUES (?);
                """;
        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setString(1, rekeningnummer.getRekeningnummer());
            try {
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                var sqlSelect = """ 
                        select nummer
                        from rekeningen
                        where naam = ?
                        """;
                try (var statementSelect = connection.prepareStatement(sqlSelect)) {
                    statementSelect.setString(1, rekeningnummer.getRekeningnummer());
                    if (statementSelect.executeQuery().next()) {
                        connection.commit();
                        throw new RekeningBestaatAlException();
                    }
                    connection.commit();
                    throw ex;
                }
            }
        }
    }
}
