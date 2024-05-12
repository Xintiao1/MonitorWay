alter table mw_topo_editor_link add  is_asset VARCHAR(2);
alter table mw_topo_editor_node add  is_asset VARCHAR(2);
insert into mw_module_perm_mapper(module_id,perm_id) VALUES (176,1),(176,2) ,(176,3) ,(176,4) ,(176,5) ,(176,6);

insert into mw_module_perm_mapper(module_id,perm_id) VALUES (177,1),(177,2) ,(177,3) ,(177,4) ,(177,5) ,(177,6);
alter table mw_topo_editor_link add  is_asset VARCHAR(2);
alter table mw_topo_editor_node add  is_asset VARCHAR(2);