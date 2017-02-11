package org.hiero.sketch.spreadsheet;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hiero.sketch.dataset.api.IJson;
import org.hiero.sketch.dataset.api.ISketch;
import org.hiero.sketch.dataset.api.PartialResult;
import org.hiero.sketch.table.Schema;
import org.hiero.sketch.table.api.ITable;
import org.hiero.utils.Randomness;
import rx.Observable;

/**
 * A sketch which retrieves the Schema and size of a distributed table.
 * Two schemas can be added only if they are identical.
 * We use the empty schema to represent a zero.
 */
public class SummarySketch implements ISketch<ITable, SummarySketch.TableSummary> {
    public static class TableSummary implements IJson {
        public TableSummary(Schema schema, long rowCount) {
            this.schema = schema;
            this.rowCount = rowCount;
        }

        public TableSummary() {
            this.schema = null;
            this.rowCount = 0;
        }

        public TableSummary add(TableSummary other) {
            Schema s = this.schema;
            if (this.schema == null)
                 s = other.schema;
            else if (!this.schema.equals(other.schema))
                throw new RuntimeException("Schemas differ");
            return new TableSummary(s, this.rowCount + other.rowCount);
        }

        public final Schema schema;
        public final long   rowCount;

        @Override
        public JsonElement toJsonTree() {
            JsonObject result = new JsonObject();
            result.addProperty("rowCount", this.rowCount);
            if (this.schema == null)
                result.add("schema", JsonNull.INSTANCE);
            else
                result.add("schema", this.schema.toJsonTree());
            return result;
        }
    }

    @NonNull
    @Override
    public TableSummary zero() {
        return new TableSummary();
    }

    @NonNull
    @Override
    public TableSummary add(@NonNull TableSummary left, @NonNull TableSummary right) {
        return left.add(right);
    }

    @NonNull
    @Override
    public Observable<PartialResult<TableSummary>> create(@NonNull ITable data) {
        try {
            Thread.sleep(1000 * Randomness.getInstance().nextInt(5));
        } catch (InterruptedException unused) {}
        TableSummary ts = new TableSummary(data.getSchema(), data.getNumOfRows());
        return this.pack(ts);
    }
}