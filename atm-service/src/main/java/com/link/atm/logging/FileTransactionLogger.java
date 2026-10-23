package com.link.atm.logging;

import com.link.atm.util.MaskUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FileTransactionLogger implements TransactionLogger {

    public static final String LOGGER_NAME = "TRANSACTION_LOG";

    private static final Logger TRANSACTION_LOG = LoggerFactory.getLogger(LOGGER_NAME);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final String SEPARATOR = "|";

    @Override
    public void log(String operation, String cardNumber, String accountOrCbu, BigDecimal amount,
                     Result result, String detail, BigDecimal resultingBalance) {
        TRANSACTION_LOG.info(buildLine(operation, cardNumber, accountOrCbu, amount, result, detail, resultingBalance));
    }

    private String buildLine(String operation, String cardNumber, String accountOrCbu, BigDecimal amount,
                              Result result, String detail, BigDecimal resultingBalance) {
        StringBuilder sb = new StringBuilder();
        sb.append(OffsetDateTime.now().format(TIMESTAMP_FORMAT)).append(SEPARATOR);
        sb.append(operation).append(SEPARATOR);
        sb.append("card=").append(fieldOrDash(MaskUtil.maskCardNumber(cardNumber))).append(SEPARATOR);
        sb.append("account=").append(fieldOrDash(accountOrCbu)).append(SEPARATOR);
        sb.append("amount=").append(amount == null ? "-" : amount.toPlainString()).append(SEPARATOR);
        sb.append("result=").append(result).append(SEPARATOR);
        sb.append("detail=").append(fieldOrDash(detail)).append(SEPARATOR);
        sb.append("resultingBalance=").append(resultingBalance == null ? "-" : resultingBalance.toPlainString());
        return sb.toString();
    }

    private String fieldOrDash(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value;
    }
}
