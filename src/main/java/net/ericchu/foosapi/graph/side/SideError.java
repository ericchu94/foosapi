package net.ericchu.foosapi.graph.side;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import net.ericchu.foosapi.graph.match.ImmutableMatchError;
import org.immutables.value.Value;

import java.util.NoSuchElementException;

@Value.Immutable
public abstract class SideError {
    abstract SideErrorCode code();

    abstract String message();

    public static SideError of(Throwable t) {
        if (t == null)
            return null;

        return ImmutableSideError.builder().code(SideErrorCode.GENERIC).message(t.getMessage()).build();
    }
}
