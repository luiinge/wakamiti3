create table plan_node(
    node_id UUID not null,
    type tinyint not null,
    name varchar(300),
    language varchar(10),
    id varchar(100),
    source varchar(300),
    keyword varchar(100),
    description clob,
    display_name_pattern varchar(100),
    data_table clob,
    document clob,
    document_type varchar(100),
    primary key (id)
);
create table plan_node_tag(
    node_id UUID not null,
    tag VARCHAR(100) not null,
    primary key (node_id, tag),
    foreign key (node_id) references plan_node(node_id) on delete cascade
);
create table plan_node_property(
    node_id UUID not null,
    key VARCHAR(100) not null,
    value VARCHAR(100),
    primary key (node_id, key),
    foreign key (node_id) references plan_node(node_id) on delete cascade
);
create table plan_node_hierarchy(
    node_id UUID not null,
    root UUID not null,
    path ARRAY UUID array default array[],
    sibling_order smallint not null,
    primary key (node_id),
    foreign key (node_id) references plan_node(node_id) on delete cascade,
    foreign key (root) references plan_node(node_id) on delete cascade
);
create index idx_plan_node_hierarchy_root on plan_node_hierarchy(root);