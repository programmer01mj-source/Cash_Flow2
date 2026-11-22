import docx

doc = docx.Document()

md_path = r"C:/Users/COMADMIN/AndroidStudioProjects/Cashflow2/.artifacts/4111e743-660e-4fe4-9b38-43a53a7c037f/test_documentation.artifact.md"
docx_path = r"C:/Users/COMADMIN/AndroidStudioProjects/Cashflow2/test_documentation.docx"

with open(md_path, "r", encoding="utf-8") as f:
    lines = f.readlines()

in_table = False
table_data = []

for line in lines:
    stripped = line.strip()
    if stripped.startswith("|") and stripped.endswith("|"):
        if not in_table:
            in_table = True
            table_data = []
        # parse table row
        cells = [c.strip() for c in stripped.split("|")[1:-1]]
        table_data.append(cells)
        continue
    else:
        if in_table:
            # flush table
            if table_data:
                # filter out separator rows like | :--- | :--- |
                valid_rows = []
                for row in table_data:
                    if all(cell.replace(":", "").replace("-", "").strip() == "" for cell in row):
                        continue
                    valid_rows.append(row)

                if valid_rows:
                    table = doc.add_table(rows=len(valid_rows), cols=len(valid_rows[0]))
                    table.style = 'Table Grid'
                    for r_idx, row in enumerate(valid_rows):
                        for c_idx, cell_text in enumerate(row):
                            table.cell(r_idx, c_idx).text = cell_text
            in_table = False
            table_data = []

    if stripped.startswith("# "):
        doc.add_heading(stripped[2:], level=1)
    elif stripped.startswith("## "):
        doc.add_heading(stripped[3:], level=2)
    elif stripped.startswith("### "):
        doc.add_heading(stripped[4:], level=3)
    elif stripped.startswith("- ") or stripped.startswith("* "):
        doc.add_paragraph(stripped[2:], style='List Bullet')
    elif stripped.startswith("1. ") or stripped.startswith("2. ") or stripped.startswith("3. ") or stripped.startswith("4. ") or stripped.startswith("5. "):
        doc.add_paragraph(stripped[3:], style='List Number')
    elif stripped == "---":
        doc.add_paragraph().add_run("__________________________________________________")
    elif stripped:
        doc.add_paragraph(stripped)

# flush if file ended with table
if in_table and table_data:
    valid_rows = [row for row in table_data if not all(c.replace(":", "").replace("-", "").strip() == "" for c in row)]
    if valid_rows:
        table = doc.add_table(rows=len(valid_rows), cols=len(valid_rows[0]))
        table.style = 'Table Grid'
        for r_idx, row in enumerate(valid_rows):
            for c_idx, cell_text in enumerate(row):
                table.cell(r_idx, c_idx).text = cell_text

doc.save(docx_path)
print("Successfully converted to", docx_path)
