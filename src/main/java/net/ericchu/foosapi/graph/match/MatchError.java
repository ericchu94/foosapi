package net.ericchu.foosapi.graph.match;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import org.immutables.value.Value;

@Value.Immutable
public abstract class MatchError {
    abstract MatchErrorCode code();

    abstract String message();

    public static MatchError of(Throwable t) {
        if (t == null)
            return null;

        if (t instanceof MongoBulkWriteException) {
            MongoBulkWriteException ex = (MongoBulkWriteException) t;
            BulkWriteError bulkWriteError = ex.getWriteErrors().get(0);
            MatchErrorCode code = bulkWriteError.getCode() == 11000 ? MatchErrorCode.DUPLICATE : MatchErrorCode.GENERIC;
            return ImmutableMatchError.builder().code(code).message(bulkWriteError.getMessage()).build();
        }

        return ImmutableMatchError.builder().code(MatchErrorCode.GENERIC).message(t.getMessage()).build();
    }
}
