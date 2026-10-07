"""0.4.2: deterministic pipe layers, plain 5% glass, inset substrate sides."""
# PNG alpha has 8-bit precision: 13/255 is the nearest value to 5%.
for prefix,color in [('',(0xdb,0xdb,0xdb)),('hostile_', (0x58,0x30,0x7a))]:
 pixels=[[(*color,13)]*16 for _ in range(16)]
 for mask in range(16):png(prefix+'glass_connected_'+str(mask),pixels)
 png(prefix+'glass_clear',pixels)
for p in (ROOT/'assets'/ID/'textures/block').glob('*glass*.png'):
 color=(0x58,0x30,0x7a) if p.stem.startswith('hostile_') else (0xdb,0xdb,0xdb)
 png(p.stem,[[(*color,13)]*16 for _ in range(16)])
# Multipart apply arrays are RANDOM choices, not layers. Each layer gets its
# own multipart entry with the same predicate, so both always render.
for name in ('swim_tube','passive_pipe','hostile_pipe'):
 p=ROOT/'assets'/ID/'blockstates'/f'{name}.json';state=json.loads(p.read_text());parts=[]
 for part in state['multipart']:
  for apply in part['apply'] if isinstance(part['apply'],list) else [part['apply']]:parts.append({**part,'apply':apply})
 state['multipart']=parts;p.write_text(json.dumps(state,indent=2)+'\n')
# Restore actual translucent glass with uniform color and no diagonal lines.
for p in (ROOT/'assets'/ID/'models').rglob('*.json'):
 m=json.loads(p.read_text());changed=False
 for k,v in m.get('textures',{}).items():
  sprite=v.get('sprite','') if isinstance(v,dict) else v
  if k!='particle' and isinstance(sprite,str) and ':block/' in sprite and 'glass' in sprite:
   m['textures'][k]={'sprite':sprite,'force_translucent':True};changed=True
 if changed:p.write_text(json.dumps(m,indent=2)+'\n')
# Floor sides stay behind the glass. The top is inset only 0.12 model units: enough to keep it inside the block silhouette at oblique angles while leaving sub-pixel seams between connected tanks.
for i,s in enumerate(soils[1:],1):
 body=box([.6,.6,.6],[15.4,2,15.4],'soil');body['faces'].pop('up')
 top={'from':[FLOOR_TOP_INSET,2,FLOOR_TOP_INSET],'to':[16-FLOOR_TOP_INSET,2,16-FLOOR_TOP_INSET],'faces':{'up':{'texture':'#soil','uv':[0,0,16,16]}}}
 model(f'soil_{i}',[body,top],{'soil':'minecraft:block/'+s,'particle':'minecraft:block/'+s})
meta=json.loads(metapath.read_text());meta['version']='0.4.2';metapath.write_text(json.dumps(meta,indent=2)+'\n')
