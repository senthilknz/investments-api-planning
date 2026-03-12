---
name: drawio
description: >
  Generates polished, valid draw.io XML diagram files (.drawio) that open directly in draw.io /
  diagrams.net. Use this skill whenever the user wants to create or visualize any kind of diagram —
  flowcharts, architecture diagrams, sequence diagrams, entity-relationship diagrams (ERDs), or mind
  maps. Trigger on phrases like "create a diagram", "draw a flowchart", "make an architecture
  diagram", "visualize this flow", "draw.io", "diagrams.net", "I need a diagram for", or whenever
  the user describes a system, process, data model, or workflow and wants it visualized. Even if
  they don't say "draw.io" explicitly — if they want a diagram that could be opened or edited
  visually, use this skill.
---

# draw.io Diagram Generator

You generate `.drawio` XML files that open directly in draw.io / diagrams.net. The goal is
diagrams that are immediately usable: well-spaced, consistently colored, and correctly structured.

## Step 1 — Pick the right diagram type

| What the user wants to show | Diagram type |
|-----------------------------|--------------|
| A process, workflow, or decision logic | Flowchart |
| System components and their connections | Architecture |
| Time-ordered interactions between actors | Sequence |
| Database tables and their relationships | ERD |
| A central idea with branching sub-topics | Mind Map |

When the description is ambiguous, make a reasonable choice and briefly note it — don't block on
clarification for straightforward requests.

## Step 2 — Plan before coding

Sketch the layout mentally: how many nodes, rough positions, which direction flows (top-to-bottom
or left-to-right). This prevents tangled edges and cramped nodes in the final XML. Assign IDs now
(e.g. `n1`, `n2`, `e1`, `e2`) so the XML stays readable.

## Step 3 — Write the XML

### File skeleton

Every `.drawio` file wraps a single `<mxGraphModel>` inside `<diagram>` inside `<mxfile>`:

```xml
<mxfile host="app.diagrams.net">
  <diagram name="Page-1">
    <mxGraphModel dx="1422" dy="762" grid="1" gridSize="10" guides="1"
                  tooltips="1" connect="1" arrows="1" fold="1"
                  page="1" pageScale="1" pageWidth="1169" pageHeight="827"
                  math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
        <!-- all your nodes and edges go here, parent="1" unless inside a container -->
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
```

### Shape styles

**Rounded rectangle (process / generic node):**
```
rounded=1;whiteSpace=wrap;html=1;arcSize=10;
```

**Terminal (start/end — pill shape):**
```
rounded=1;whiteSpace=wrap;html=1;arcSize=50;
```

**Diamond (decision):**
```
rhombus;whiteSpace=wrap;html=1;
```

**Parallelogram (I/O):**
```
shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fixedSize=1;size=15;
```

**Cylinder (database / storage):**
```
shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;
```

**Cloud (external service):**
```
shape=mxgraph.network.cloud;whiteSpace=wrap;html=1;
```

**Person / actor:**
```
shape=mxgraph.basic.person;whiteSpace=wrap;html=1;
```

**Document:**
```
shape=mxgraph.flowchart.document;whiteSpace=wrap;html=1;
```

### Edges

**Default directed arrow:**
```
edgeStyle=orthogonalEdgeStyle;html=1;
```

**Dashed / async:**
```
edgeStyle=orthogonalEdgeStyle;html=1;dashed=1;
```

**No arrowhead (lifeline, boundary line):**
```
endArrow=none;startArrow=none;html=1;
```

**Bidirectional:**
```
edgeStyle=orthogonalEdgeStyle;html=1;startArrow=block;startFill=0;endArrow=block;endFill=0;
```

**ERD relationship:**
```
edgeStyle=entityRelationEdgeStyle;html=1;endArrow=ERmany;startArrow=ERone;
```
Available ERD arrow values: `ERone`, `ERmany`, `ERoneToMany`, `ERzeroToOne`, `ERzeroToMany`, `ERmandOne`.

### Cell templates

**Vertex node:**
```xml
<mxCell id="n1" value="Label"
        style="rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;"
        vertex="1" parent="1">
  <mxGeometry x="200" y="160" width="160" height="60" as="geometry"/>
</mxCell>
```

**Edge:**
```xml
<mxCell id="e1" value="Yes"
        style="edgeStyle=orthogonalEdgeStyle;html=1;"
        edge="1" source="n1" target="n2" parent="1">
  <mxGeometry relative="1" as="geometry"/>
</mxCell>
```

**Container (swimlane / group):**
```xml
<mxCell id="c1" value="Backend"
        style="swimlane;startSize=30;fillColor=#f5f5f5;strokeColor=#666666;fontColor=#333333;"
        vertex="1" parent="1">
  <mxGeometry x="60" y="120" width="300" height="200" as="geometry"/>
</mxCell>
<!-- child nodes use parent="c1" and relative x/y within container -->
```

## Color Palettes

Use color to encode meaning, not just decoration. Pick one palette per diagram unless you're
deliberately signaling different semantic roles (e.g., green = success path, red = error path).

| Semantic role       | fillColor | strokeColor | fontColor |
|---------------------|-----------|-------------|-----------|
| Primary / action    | `#dae8fc` | `#6c8ebf`   | `#000000` |
| Success / happy path| `#d5e8d4` | `#82b366`   | `#000000` |
| Warning / decision  | `#ffe6cc` | `#d6b656`   | `#000000` |
| Error / failure     | `#f8cecc` | `#b85450`   | `#000000` |
| Neutral / context   | `#f5f5f5` | `#666666`   | `#333333` |
| External system     | `#e1d5e7` | `#9673a6`   | `#000000` |
| Database / storage  | `#fff2cc` | `#d6b656`   | `#000000` |
| Dark header         | `#647687` | `#314354`   | `#ffffff` |

## Layout Guidelines

Good layout makes diagrams readable at a glance:

- **Flowcharts**: top-to-bottom. Start at y=80, space nodes 80–100px apart vertically.
- **Architecture**: left-to-right or layered (frontend → backend → data). Group layers in containers.
- **Sequence diagrams**: actors across the top (y=40, spaced 200px apart), time flows down. Lifelines are vertical dashed lines.
- **ERDs**: grid layout. Tables sized to fit content (30px header + 30px per column). Space tables 60px apart.
- **Mind maps**: center node at ~(500, 400), first-level branches at radius ~200, second-level at ~350.
- **Grid alignment**: place all nodes on a 40px or 80px grid. Typical node sizes: 120×60 (small), 160×60 (medium), 200×80 (large).
- **Spacing**: minimum 40px gap between unconnected nodes; 80px between major sections or layers.

## Diagram-Specific Guidance

### Flowcharts

Shape conventions:
- **Terminal** (start/end): pill/rounded rectangle, neutral gray
- **Process**: rounded rectangle, primary blue
- **Decision**: diamond, warning orange
- **I/O**: parallelogram, neutral gray
- **Error path edges**: red color (`strokeColor=#b85450`)
- **Happy path edges**: default or green

Always label decision edges (Yes/No, or the specific condition). For complex flows with parallel
branches, group related paths in a swimlane container.

### Architecture Diagrams

Organize components by layer or system boundary using swimlane containers:
- Label each container (e.g., "Client", "API Layer", "Services", "Data Layer", "External")
- Use arrows to show data flow direction; label with protocol/data type when helpful (REST, gRPC, SQL, events)
- External systems (Stripe, S3, third-party APIs): use cloud shape or external-system purple palette
- Databases: cylinder shape, database-yellow palette
- For AWS/GCP/Azure: use `shape=mxgraph.aws4.resourceIcon;resIcon=mxgraph.aws4.<service>` for official icons when the user mentions specific cloud services

### Sequence Diagrams

Layout:
1. Place actor boxes across the top row
2. Draw vertical lifeline (dashed, no arrow) from each actor downward
3. Draw horizontal message arrows between lifelines, numbered top to bottom
4. For activations, add a narrow tall rectangle on the lifeline

Lifeline style: `endArrow=none;dashed=1;html=1;strokeColor=#666666;`
Activation box style: `rounded=0;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;`

Use `«create»` / `«destroy»` for object lifecycle, `loop` / `alt` swimlane containers for
conditional or repeated groups.

### ERDs

Each table = a swimlane container. Header row = table name (bold). Each column = a child cell.

Table container style:
```
swimlane;startSize=30;fillColor=#dae8fc;strokeColor=#6c8ebf;fontStyle=1;
```

Column cell style:
```
text;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;spacingLeft=4;
overflow=hidden;rotatable=0;points=[[0,0.5],[1,0.5]];portConstraint=eastwest;
```

Prefix columns: `PK` for primary keys, `FK` for foreign keys. Size each table to fit: height = 30 (header) + 30 × (number of columns).

Connect related tables with ERD edges (`edgeStyle=entityRelationEdgeStyle`) using the appropriate
cardinality arrows. Edges connect the FK column cell to the PK column cell of the referenced table.

### Mind Maps

Central topic: large oval or rounded rectangle, dark-header palette, placed at center.
First-level branches: medium rounded rectangles, primary-blue palette.
Second-level branches: smaller rounded rectangles, neutral or success-green palette.

Use curved edges without arrowheads: `edgeStyle=elbowEdgeStyle;curved=1;endArrow=none;`

Spread branches evenly: left half / right half, or top/bottom quadrants. Avoid all branches on one side.

## Step 4 — Save the file

Save the completed XML as `<descriptive-name>.drawio` in the current working directory. The filename
should reflect the content (e.g., `user-auth-flow.drawio`, `payments-architecture.drawio`,
`blog-erd.drawio`).

Also briefly tell the user: what file was saved, what diagram type was used, and how to open it
(drag into draw.io / diagrams.net or File → Open).

## Quality checks before saving

- All cell IDs are unique strings
- Every edge's `source` and `target` reference existing vertex IDs
- Labels are concise — wrap long text with `whiteSpace=wrap`
- Color is used consistently (same semantic meaning = same color)
- Nodes are grid-aligned; no nodes at x/y coordinates greater than 2000 unless the diagram is genuinely large
- XML is well-formed (all tags closed, attributes quoted)
