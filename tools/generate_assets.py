"""Generate original aquarium geometry and data. No external Python packages needed."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
ID='linked_aquariums'
def write(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n')
def model(name,elements,textures):write(Path('assets')/ID/'models/block'/f'{name}.json',{'textures':textures,'elements':[e for e in elements if e.get('faces')]})
def box(a,b,texture,cull=False):
 return {'from':a,'to':b,'faces':{d:{'texture':'#'+texture,**({'cullface':d} if cull else {})} for d in ['down','up','north','south','west','east']}}
colors='white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split()
soils=['','sand','red_sand','gravel']+[c+'_concrete_powder' for c in colors]
plants='seagrass kelp sea_pickle short_grass fern dandelion poppy blue_orchid allium azure_bluet red_tulip orange_tulip white_tulip pink_tulip oxeye_daisy cornflower lily_of_the_valley wither_rose'.split()
woods='oak spruce birch jungle acacia dark_oak mangrove cherry bamboo pale_oak crimson warped'.split()
decors=['']+plants+['stone_button','polished_blackstone_button']+[w+'_fence' for w in woods]
assert len(soils)==20 and len(decors)==33
tex={'glass':{'sprite':ID+':block/glass_clear','force_translucent':True},'particle':ID+':block/glass_clear'}
water_tex={'water':{'sprite':ID+':block/contained_water','force_translucent':True},'particle':ID+':block/contained_water'}
directions=['down','up','north','south','west','east']
slabs=[([0,0,0],[16,.5,16]),([0,15.5,0],[16,16,16]),([0,0,0],[16,16,.5]),([0,0,15.5],[16,16,16]),([0,0,0],[.5,16,16]),([15.5,0,0],[16,16,16])]
multipart=[]
# Face UV axes map texture left/right/top/bottom to world directions.
uv_axes={
 'down':['west','east','south','north'], 'up':['west','east','north','south'],
 'north':['east','west','up','down'], 'south':['west','east','up','down'],
 'west':['north','south','up','down'], 'east':['south','north','up','down']}
opposite={'down':'up','up':'down','north':'south','south':'north','west':'east','east':'west'}
for d,(a,b) in zip(directions,slabs):
 for mask in range(16):
  linked={uv_axes[d][i]:bool(mask&(1<<i)) for i in range(4)}
  backmask=sum((1<<i) for i,v in enumerate(uv_axes[opposite[d]]) if linked[v])
  textures={'front':{'sprite':ID+':block/glass_connected_'+str(mask),'force_translucent':True},'back':{'sprite':ID+':block/glass_connected_'+str(backmask),'force_translucent':True},**tex}
  faces={d:{'texture':'#front','cullface':d},opposite[d]:{'texture':'#back'}}
  for edge in linked:
   if not linked[edge]:faces[edge]={'texture':'#glass'}
  model('glass_'+d+'_'+str(mask),[{'from':a,'to':b,'faces':faces}],textures)
  multipart.append({'when':{d:'false',**{k:str(v).lower() for k,v in linked.items()}},'apply':{'model':ID+':block/glass_'+d+'_'+str(mask)}})
 # Aquarium water is inset behind the glass and reaches the whole chamber height.
 water_box=box([.55,.55,.55],[15.45,15.45,15.45],'water')
 water_box['faces']={d:water_box['faces'][d]}
 model('water_'+d,[water_box],water_tex)
 multipart.append({'when':{d:'false'},'apply':{'model':ID+':block/water_'+d}})
for i,s in enumerate(soils[1:],1):
 model('soil_'+str(i),[box([0,.5,0],[16,2,16],'soil',True)],{'soil':'minecraft:block/'+s,'particle':'minecraft:block/'+s})
 multipart.append({'when':{'soil':str(i),'down':'false'},'apply':{'model':ID+':block/soil_'+str(i)}})
for i,s in enumerate(decors[1:],1):
 if s.endswith('_button'):
  rock='polished_blackstone' if s.startswith('polished') else 'stone'
  elements=[box([3,2,4],[7,4,8],'decor'),box([9,2,9],[13,3.5,12],'decor')];texture='minecraft:block/'+rock
 elif s.endswith('_fence'):
  wood=s.removesuffix('_fence');texture='minecraft:block/'+(wood+'_stem' if wood in ('crimson','warped') else 'bamboo_block' if wood=='bamboo' else wood+'_log')
  elements=[box([2,2,5],[14,6,9],'decor')]
 elif s=='sea_pickle':
  texture='minecraft:block/sea_pickle';elements=[box([5,2,5],[8,6,8],'decor'),box([10,2,9],[12,5,11],'decor')]
 else:
  texture='minecraft:block/'+('kelp' if s=='kelp' else s)
  elements=[]
  for angle in [45,-45]:
   elements.append({'from':[3,2,8],'to':[13,13,8],'rotation':{'origin':[8,2,8],'axis':'y','angle':angle,'rescale':True},'faces':{'north':{'texture':'#decor','uv':[0,0,16,16],**({'tintindex':0} if s in ('short_grass','fern') else {})},'south':{'texture':'#decor','uv':[0,0,16,16],**({'tintindex':0} if s in ('short_grass','fern') else {})}}})
 model('decor_'+str(i),elements,{'decor':texture,'particle':texture})
 multipart.append({'when':{'decor':str(i)},'apply':{'model':ID+':block/decor_'+str(i)}})
write(Path('assets')/ID/'blockstates/aquarium.json',{'multipart':multipart})
model('aquarium',[box(*slabs[i],'glass') for i in range(6)],tex)
# The tube interior is a central chamber plus an arm toward each linked face.
variants={}
coords=[0,2,14,16]
steps=[(0,-1,0),(0,1,0),(0,0,-1),(0,0,1),(-1,0,0),(1,0,0)]
for mask in range(64):
 cells={(1,1,1)}
 for i,v in enumerate(steps):
  if mask&(1<<i):cells.add(tuple(1+x for x in v))
 elements=[]
 for x,y,z in cells:
  a=[coords[x],coords[y],coords[z]];b=[coords[x+1],coords[y+1],coords[z+1]]
  faces={}
  for i,(dx,dy,dz) in enumerate(steps):
   if (x+dx,y+dy,z+dz) in cells:continue
   # Branch ends meet another module and have no glass cap.
   if (i==0 and y==0) or (i==1 and y==2) or (i==2 and z==0) or (i==3 and z==2) or (i==4 and x==0) or (i==5 and x==2):continue
   faces[directions[i]]={'texture':'#glass'}
  elements.append({'from':a,'to':b,'faces':faces})
 water_elements=[]
 for element in elements:
  a=element['from'];b=element['to'];wa=[2.2 if v==2 else 13.8 if v==14 else v for v in a];wb=[2.2 if v==2 else 13.8 if v==14 else v for v in b]
  water_elements.append({'from':wa,'to':wb,'faces':{d:{'texture':'#water'} for d in element['faces']}})
 model('tube_'+str(mask),elements,tex)
 model('tube_water_'+str(mask),water_elements,water_tex)
 variants[','.join(d+'='+str(bool(mask&(1<<i))).lower() for i,d in enumerate(directions))]={'model':ID+':block/tube_'+str(mask)}
tube_parts=[]
for mask in range(64):
 tube_parts.append({'when':{d:str(bool(mask&(1<<i))).lower() for i,d in enumerate(directions)},'apply':[{'model':ID+':block/tube_'+str(mask)},{'model':ID+':block/tube_water_'+str(mask)}]})
# Glass rings seal the tank's large face around a smaller tube opening.
for i,d in enumerate(directions):
 axis=1 if i<2 else 2 if i<4 else 0
 outer=0 if i%2==0 else 16
 axes=[n for n in range(3) if n!=axis]
 elements=[]
 for u0,v0,u1,v1 in [(0,0,2,16),(14,0,16,16),(2,0,14,2),(2,14,14,16)]:
  a=[0,0,0];b=[0,0,0];a[axis]=max(0,outer-.2);b[axis]=min(16,outer+.2)
  a[axes[0]]=u0;b[axes[0]]=u1;a[axes[1]]=v0;b[axes[1]]=v1
  elements.append(box(a,b,'glass'))
 model('tube_ring_'+d,elements,tex)
 tube_parts.append({'when':{'tank_'+d:'true'},'apply':{'model':ID+':block/tube_ring_'+d}})
write(Path('assets')/ID/'blockstates/swim_tube.json',{'multipart':tube_parts})
write(Path('assets')/ID/'items/aquarium.json',{'model':{'type':'minecraft:model','model':ID+':block/aquarium'}})
write(Path('assets')/ID/'items/swim_tube.json',{'model':{'type':'minecraft:model','model':ID+':block/tube_0'}})
write(Path('assets')/ID/'lang/en_us.json',{'block.'+ID+'.aquarium':'Aquarium','block.'+ID+'.swim_tube':'Swim Tube'})
for name in ['aquarium','swim_tube']:
 pools=[{'rolls':1,'entries':[{'type':'minecraft:item','name':ID+':'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]
 if name=='aquarium':
  for prop,values in [('soil',soils),('decor',decors)]:
   for i,s in enumerate(values[1:],1):
    pools.append({'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:'+s}],'conditions':[{'condition':'minecraft:block_state_property','block':ID+':aquarium','properties':{prop:str(i)}},{'condition':'minecraft:survives_explosion'}]})
 write(Path('data')/ID/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':pools})
write(Path('data')/ID/'recipe/aquarium.json',{'type':'minecraft:crafting_shaped','pattern':['GGG','G G','III'],'key':{'G':'minecraft:glass','I':'minecraft:iron_nugget'},'result':{'id':ID+':aquarium','count':4}})
write(Path('data')/ID/'recipe/swim_tube.json',{'type':'minecraft:crafting_shaped','pattern':['GGG','   ','GGG'],'key':{'G':'minecraft:glass'},'result':{'id':ID+':swim_tube','count':6}})
write(Path('data/minecraft/tags/block/mineable/pickaxe.json'),{'replace':False,'values':[ID+':aquarium',ID+':swim_tube']})
write(Path('fabric.mod.json'),{'schemaVersion':1,'id':ID,'version':'0.1.1','name':'Linked Aquariums','description':'Connected glass aquariums, real fish, substrate, decorations and swim tubes.','authors':['Casz'],'environment':'*','entrypoints':{'main':['dev.casz.aquarium.AquariumMod'],'client':['dev.casz.aquarium.AquariumClient']},'depends':{'fabricloader':'>=0.19.5','minecraft':'26.2','java':'>=25','fabric-api':'>=0.158.0'}})
# Original RGBA glass, contained-water and invisible-fluid sprites, using stdlib.
import struct,zlib
def png(name,pixels):
 p=ROOT/'assets'/ID/'textures/block'/f'{name}.png';p.parent.mkdir(parents=True,exist_ok=True)
 raw=b''.join(b'\x00'+bytes(v for pixel in row for v in pixel) for row in pixels)
 def chunk(k,v):return struct.pack('>I',len(v))+k+v+struct.pack('>I',zlib.crc32(k+v)&0xffffffff)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))
for mask in range(16):
 pixels=[]
 for y in range(16):
  row=[]
  for x in range(16):
   edge=(x==0 and not mask&1) or (x==15 and not mask&2) or (y==0 and not mask&4) or (y==15 and not mask&8)
   row.append((67,123,138,190) if edge else (148,210,230,38) if x-y in (2,3) else (172,218,232,12))
  pixels.append(row)
 png('glass_connected_'+str(mask),pixels)
png('glass_clear',[[(172,218,232,12) for x in range(16)] for y in range(16)])
png('contained_water',[[(44,126,211,62 if (x+y)%7==0 else 48) for x in range(16)] for y in range(16)])
png('invisible_water',[[(0,0,0,0) for x in range(16)] for y in range(16)])
write(Path('data/minecraft/tags/fluid/water.json'),{'replace':False,'values':[ID+':contained_water']})
# Render-only display states: four rock/log shapes and animated chest poses.
import copy
hidden={}
for kind in range(35):
 for variant in range(4):
  elements=[];textures={'decor':'minecraft:block/stone','particle':'minecraft:block/stone'}
  if 1<=kind<=32:
   base=json.loads((ROOT/'assets'/ID/'models/block'/f'decor_{kind}.json').read_text());elements=copy.deepcopy(base['elements']);textures=base['textures']
   if kind in (19,20):
    shapes=[[(3,2,4,7,4,8),(9,2,9,13,3.5,12)],[(4,2,4,12,8,12)],[(2,2,3,6,5,7),(8,2,5,13,6,10),(5,2,11,9,4,14)],[(2,2,4,14,3.5,12),(5,3.5,6,11,5,10)]]
    elements=[box(list(s[:3]),list(s[3:]),'decor') for s in shapes[variant]]
   elif kind>=21:
    shapes=[[(2,2,5,14,6,9)],[(2,2,3,14,6,7),(3,2,10,13,6,14),(4,6,6,12,10,10)],[(2,2,6,14,6,10),(8,3,3,12,7,7),(4,3,9,8,7,14)],[(2,2,4,14,3,12),(2,7,4,14,8,12),(2,3,4,14,7,5),(2,3,11,14,7,12)]]
    elements=[box(list(s[:3]),list(s[3:]),'decor') for s in shapes[variant]]
  elif kind==33:
   textures={'decor':'minecraft:block/prismarine','particle':'minecraft:block/prismarine'}
   elements=[box([7.4,1,7.4],[8.6,10,8.6],'decor'),box([4,10,7.2],[12,11,8.8],'decor')]+[box([x,10,7.2],[x+1.2,14,8.8],'decor') for x in (4,7.4,10.8)]
   if variant in (1,2):
    for e in elements:e['rotation']={'origin':[8,1,8],'axis':'z','angle':-22.5 if variant==1 else 22.5}
   if variant==3:
    for e in elements:
     a,b=e['from'],e['to'];e['from']=[a[1],a[0]-5,a[2]];e['to']=[b[1],b[0]-5,b[2]]
  elif kind==34:
   wood='spruce' if variant>=2 else 'oak';textures={'decor':'minecraft:block/'+wood+'_planks','latch':'minecraft:block/gold_block','particle':'minecraft:block/'+wood+'_planks'}
   body=box([3,1,3],[13,6,13],'decor');lid=box([3,6,3],[13,8,13],'decor');latch=box([7,5,2.7],[9,7,3.3],'latch')
   if variant%2:
    lid['rotation']={'origin':[8,6,13],'axis':'x','angle':-45};latch['rotation']={'origin':[8,6,13],'axis':'x','angle':-45}
   elements=[body,lid,latch]
  name=f'display_decor_{kind}_{variant}';model(name,elements,textures);hidden[f'kind={kind},model={variant}']={'model':ID+':block/'+name}
write(Path('assets')/ID/'blockstates/decor_model.json',{'variants':hidden})
write(Path('data')/ID/'loot_table/blocks/decor_model.json',{'type':'minecraft:block','pools':[]})
write(Path('assets')/ID/'models/item/creature_bucket.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/water_bucket'}})
write(Path('assets')/ID/'items/creature_bucket.json',{'model':{'type':'minecraft:model','model':ID+':item/creature_bucket'}})
langpath=ROOT/'assets'/ID/'lang/en_us.json';lang=json.loads(langpath.read_text());lang['item.'+ID+'.creature_bucket']='Aquarium Creature Bucket';langpath.write_text(json.dumps(lang,indent=2)+'\n')
metapath=ROOT/'fabric.mod.json';meta=json.loads(metapath.read_text());meta['version']='0.2.0';metapath.write_text(json.dumps(meta,indent=2)+'\n')
# Typed land enclosures: dry interiors, optional shallow contained ground water.
baseparts=json.loads((ROOT/'assets'/ID/'blockstates/aquarium.json').read_text())['multipart']
dry=[p for p in baseparts if 'water_' not in p['apply']['model'] and 'decor' not in p.get('when',{})]
model('ground_water',[box([2,2.05,2],[14,3,14],'water')],water_tex)
for hostile,prefix in ((False,'passive'),(True,'hostile')):
 parts=json.loads(json.dumps(dry));pipeparts=json.loads(json.dumps(tube_parts))
 for p in pipeparts:
  if isinstance(p['apply'],list):p['apply']=[a for a in p['apply'] if 'tube_water_' not in a['model']]
 if hostile:
  def tint_reference(ref):
   name=ref.split(':block/')[-1]
   if name.startswith(('glass_','tube_')):
    src=json.loads((ROOT/'assets'/ID/'models/block'/f'{name}.json').read_text())
    for k,v in src.get('textures',{}).items():
     if isinstance(v,dict) and 'glass_' in v['sprite']:v['sprite']=v['sprite'].replace(':block/glass_',':block/hostile_glass_')
     elif isinstance(v,str) and 'glass_' in v:src['textures'][k]=v.replace(':block/glass_',':block/hostile_glass_')
    write(Path('assets')/ID/'models/block'/f'hostile_{name}.json',src)
    return ID+':block/hostile_'+name
   return ref
  for p in parts:p['apply']['model']=tint_reference(p['apply']['model'])
  for p in pipeparts:
   if isinstance(p['apply'],list):
    for a in p['apply']:a['model']=tint_reference(a['model'])
   else:p['apply']['model']=tint_reference(p['apply']['model'])
  src=json.loads((ROOT/'assets'/ID/'models/block/aquarium.json').read_text())
  for v in src['textures'].values():
   if isinstance(v,dict):v['sprite']=v['sprite'].replace('glass_clear','hostile_glass_clear')
  model('hostile_terrarium',src['elements'],src['textures'])
 parts.append({'when':{'ground_water':'true','down':'false'},'apply':{'model':ID+':block/ground_water'}})
 for name,states,itemmodel in [(prefix+'_terrarium',{'multipart':parts},'hostile_terrarium' if hostile else 'aquarium'),(prefix+'_pipe',{'multipart':pipeparts},'hostile_tube_0' if hostile else 'tube_0')]:
  write(Path('assets')/ID/'blockstates'/f'{name}.json',states)
  write(Path('assets')/ID/'items'/f'{name}.json',{'model':{'type':'minecraft:model','model':ID+':block/'+itemmodel}})
  pools=[{'rolls':1,'entries':[{'type':'minecraft:item','name':ID+':'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]
  if name.endswith('terrarium'):
   for i,s in enumerate(soils[1:],1):pools.append({'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:'+s}],'conditions':[{'condition':'minecraft:block_state_property','block':ID+':'+name,'properties':{'soil':str(i)}},{'condition':'minecraft:survives_explosion'}]})
  write(Path('data')/ID/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':pools})
 write(Path('data')/ID/'recipe'/f'{prefix}_terrarium.json',{'type':'minecraft:crafting_shaped','pattern':['GGG','G G','III'],'key':{'G':'minecraft:glass','I':'minecraft:iron_ingot' if hostile else 'minecraft:oak_planks'},'result':{'id':ID+':'+prefix+'_terrarium','count':4}})
 write(Path('data')/ID/'recipe'/f'{prefix}_pipe.json',{'type':'minecraft:crafting_shaped','pattern':['GGG',' T ','GGG'],'key':{'G':'minecraft:glass','T':ID+':'+prefix+'_terrarium'},'result':{'id':ID+':'+prefix+'_pipe','count':8}})
for mask in range(16):
 pixels=[]
 for y in range(16):
  row=[]
  for x in range(16):
   edge=(x==0 and not mask&1) or (x==15 and not mask&2) or (y==0 and not mask&4) or (y==15 and not mask&8)
   row.append((75,67,98,195) if edge else (135,123,155,50))
  pixels.append(row)
 png('hostile_glass_connected_'+str(mask),pixels)
png('hostile_glass_clear',[[(135,123,155,45) for _ in range(16)] for _ in range(16)])
# Original pixel-art net item, not an external asset.
pixels=[]
for y in range(16):
 row=[]
 for x in range(16):
  hoop=((x-6)**2/25+(y-5)**2/16)
  color=(185,143,82,255) if .7<hoop<1.3 or (x==y-1 and y>=9) else (218,234,229,240) if hoop<.7 and (x%3==0 or y%3==0) else (0,0,0,0)
  row.append(color)
 pixels.append(row)
png('mob_net',pixels)
write(Path('assets')/ID/'models/item/mob_net.json',{'parent':'minecraft:item/handheld','textures':{'layer0':ID+':block/mob_net'}})
write(Path('assets')/ID/'items/mob_net.json',{'model':{'type':'minecraft:model','model':ID+':item/mob_net'}})
write(Path('data')/ID/'recipe/mob_net.json',{'type':'minecraft:crafting_shaped','pattern':[' SS',' SS','I  '],'key':{'S':'minecraft:string','I':'minecraft:stick'},'result':{'id':ID+':mob_net','count':1}})
write(Path('data')/ID/'tags/entity_type/flying.json',{'replace':False,'values':['minecraft:'+n for n in ('parrot','bee','bat','allay','phantom','ghast','vex','blaze','happy_ghast')]})
lang=json.loads(langpath.read_text());lang.update({'block.'+ID+'.'+n:title for n,title in [('passive_terrarium','Passive Terrarium'),('hostile_terrarium','Hostile Terrarium'),('passive_pipe','Passive Terrarium Pipe'),('hostile_pipe','Hostile Terrarium Pipe')]});lang['item.'+ID+'.mob_net']='Mob Net';lang['block.'+ID+'.mobitat']='Mobitat';langpath.write_text(json.dumps(lang,indent=2)+'\n')
meta=json.loads(metapath.read_text());meta['version']='0.3.0';meta['description']='Connected aquariums and passive/hostile terrariums, miniature mobs, decor and pipes.';metapath.write_text(json.dumps(meta,indent=2)+'\n')

write(Path('linked_aquariums.mixins.json'),{'required':True,'package':'dev.casz.aquarium.mixin','compatibilityLevel':'JAVA_25','mixins':['TerrariumSunMixin','TerrariumCreeperMixin'],'injectors':{'defaultRequire':1}})
meta=json.loads(metapath.read_text());meta['mixins']=['linked_aquariums.mixins.json'];metapath.write_text(json.dumps(meta,indent=2)+'\n')

exec((Path(__file__).parent/"upgrade_visuals.py").read_text())
exec((Path(__file__).parent/"fix_surfaces.py").read_text())
exec((Path(__file__).parent/"fix_layers.py").read_text())
