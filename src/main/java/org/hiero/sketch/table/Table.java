package org.hiero.sketch.table;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.hiero.sketch.table.api.*;

/**
 * This is a simple table held entirely in RAM.
 */
public class Table {
    @NonNull
    private final ISchema schema;
    @NonNull
    private final IColumn[] columns;
    @NonNull
    private final IMembershipSet members;

    public Table(@NonNull final ISchema schema,
                 @NonNull final IColumn[] columns,
                 @NonNull final IMembershipSet members) {
        this.schema = schema;
        this.columns = columns;
        this.members = members;
        for (final IColumn c : columns) {
            final int ci = schema.getColumnIndex(c.getName());
            final ColumnDescription cd = schema.getDescription(ci);
            if (!c.getDescription().equals(cd))
                throw new IllegalArgumentException("Schema mismatch " + cd.toString() +
                        " vs. " + c.getDescription().toString());
        }
    }

    public Table(@NonNull final IColumn[] columns, @NonNull final IMembershipSet members) {
        final Schema s = new Schema();
        for (final IColumn c : columns)
            s.append(c.getDescription());
        this.schema = s;
        this.columns = columns;
        this.members = members;
    }

    /**
     * Generates a table that contains only the columns referred to by subSchema,
     * and only the rows contained in IMembership Set with consecutive numbering.
     * The order among the columns is preserved.
     */
    public Table compress(@NonNull final ISubSchema subSchema,
                          @NonNull final IRowOrder rowOrder) {
        final ISchema newSchema = this.schema.project(subSchema);
        final int width = newSchema.getColumnCount();
        final IColumn[] compressedCols = new IColumn[width];
        for (int i = 0; i < width; i++) {
            final String colName = newSchema.getDescription(i).name;
            final int j = this.schema.getColumnIndex(colName);
            compressedCols[i] = this.columns[j].compress(rowOrder);
        }
        final IMembershipSet full = new FullMembership(rowOrder.getSize());
        return new Table(newSchema, compressedCols, full);
    }

    /**
     * @param rowOrder Ordered set of rows to include in the compressed table.
     * @return A compressed table containing only the rows contained in rowOrder.
     */
    public Table compress(final IRowOrder rowOrder) {
        final ISubSchema subSchema = new FullSubSchema();
        return compress(subSchema, rowOrder);
    }

    /**
     * Generates a table that contains all the columns, and only
     * the rows contained in IMembership Set members with consecutive numbering.
     */
    public Table compress() {
        final ISubSchema subSchema = new FullSubSchema();
        return compress(subSchema, this.members);
    }

    /**
     * Creates a sample Table for an input Table,
     * @param numSamples The number of samples
     * @return A table of samples.
     */
    public Table sampleTable(final int numSamples) {
        final int dataSize = this.getNumOfRows();
        final IMembershipSet sampleSet = this.members.sample(numSamples);
        return this.compress(sampleSet);
    }

    @Override
    public String toString() {
        return("Table, " + this.schema.getColumnCount() + " columns, "
                + this.members.getSize() + " rows");
    }

    public String toLongString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.toString());
        IRowIterator rowIt = this.members.getIterator();
        int nextRow = rowIt.getNextRow();
        int count = 0;
        while(nextRow != -1 && count < 100) {
            for (IColumn nextCol: columns){
                builder.append(nextCol.asString(nextRow));
            }
            builder.append("%n");
            nextRow = rowIt.getNextRow();
            count++;
        }
        return builder.toString();
    }

    public int getColumnIndex(final String colName) {
        return this.schema.getColumnIndex(colName);
    }

    public IColumn getColumn(final String colName) {
        return this.columns[this.schema.getColumnIndex(colName)];
    }

    public IColumn getColumn(final int index) {
        return this.columns[index];
    }

    public int getNumOfRows() {
        return this.members.getSize();
    }
}
