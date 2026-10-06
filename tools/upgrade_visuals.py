"""Final connected enclosure meshes and distinct inventory models (run by generator)."""
from itertools import combinations
frame_tex={'frame':ID+':block/frame','particle':ID+':block/frame'}
png('frame',[[(28,36,44,255)]*16 for _ in range(16)])
for prefix,tint in [('',(169,220,232)),('hostile_', (162,134,194))]:
 for mask in range(16):
  pixels=[]
  for y in range(16):
   row=[]
   for x in range(16):
    edge=(x==0 and not mask&1) or (x==15 and not mask&2) or (y==0 and not mask&4) or (y==15 and not mask&8)
    row.append((28,36,44,255) if edge else (*tint,70 if x-y in (2,3) else 30))
   pixels.append(row)
  png(prefix+'glass_connected_'+str(mask),pixels)
 png(prefix+'glass_clear',[[(*tint,70 if x-y in (2,3) else 30) for x in range(16)] for y in range(16)])
png('contained_water',[[(35,120+(x+y)%4*3,222,105 if (x+y)%7 else 125) for x in range(16)] for y in range(16)])
for i in range(1,20):
 p=ROOT/'assets'/ID/'models/block'/f'soil_{i}.json';m=json.loads(p.read_text());m['ambientocclusion']=False
 for e in m['elements']:
  for f in e['faces'].values():f.pop('cullface',None)
 p.write_text(json.dumps(m,indent=2)+'\n')
axes={'down':1,'up':1,'north':2,'south':2,'west':0,'east':0}
positive={'up','south','east'}
def edge_box(a,b,d,e,width=.35):
 lo=list(a);hi=list(b)
 for face in (d,e):
  axis=axes[face]
  if face in positive:lo[axis]=hi[axis]-width
  else:hi[axis]=lo[axis]+width
 return box(lo,hi,'frame')
frames=[]
for d,e in combinations(directions,2):
 if axes[d]==axes[e]:continue
 name=f'frame_{d}_{e}';model(name,[edge_box([0,0,0],[16,16,16],d,e)],frame_tex)
 frames.append({'when':{d:'false',e:'false'},'apply':{'model':ID+':block/'+name}})
waterparts=[]
for d in directions:
 for mask in range(16):
  linked={uv_axes[d][i]:bool(mask&(1<<i)) for i in range(4)};lo=[.55]*3;hi=[15.45]*3
  for edge,connected in linked.items():
   if connected:
    if edge in positive:hi[axes[edge]]=16
    else:lo[axes[edge]]=0
  e=box(lo,hi,'water');e['faces']={d:e['faces'][d]};name=f'water_{d}_{mask}';model(name,[e],water_tex)
  waterparts.append({'when':{d:'false',**{k:str(v).lower() for k,v in linked.items()}},'apply':{'model':ID+':block/'+name}})
# Tank-to-tube links keep a visible dark mounting frame around the narrow opening.
tube_mounts=[]
for d in directions:
 axis=axes[d];face=15.65 if d in positive else .35;uv=[a for a in range(3) if a!=axis];bars=[]
 for u0,v0,u1,v1 in [(0,0,.55,16),(15.45,0,16,16),(.55,0,15.45,.55),(.55,15.45,15.45,16)]:
  lo=[0,0,0];hi=[16,16,16];lo[axis]=max(0,face-.35);hi[axis]=min(16,face+.35);lo[uv[0]]=u0;hi[uv[0]]=u1;lo[uv[1]]=v0;hi[uv[1]]=v1
  bars.append(box(lo,hi,'frame'))
 model('tank_tube_mount_'+d,bars,frame_tex)
 tube_mounts.append({'when':{'tube_'+d:'true'},'apply':{'model':ID+':block/tank_tube_mount_'+d}})
for name in ('aquarium','passive_terrarium','hostile_terrarium'):
 p=ROOT/'assets'/ID/'blockstates'/f'{name}.json';m=json.loads(p.read_text());m['multipart']+=frames
 if name=='aquarium':m['multipart']+=tube_mounts
 if name=='aquarium':m['multipart']=waterparts+[p for p in m['multipart'] if ':block/water_' not in p['apply']['model']]
 p.write_text(json.dumps(m,indent=2)+'\n')
pipeframes=[]
for mask in range(64):
 base=json.loads((ROOT/'assets'/ID/'models/block'/f'tube_{mask}.json').read_text());edges=[]
 for element in base['elements']:
  for d,e in combinations(element['faces'],2):
   if axes[d]!=axes[e]:edges.append(edge_box(element['from'],element['to'],d,e))
 model(f'tube_frame_{mask}',edges,frame_tex)
 pipeframes.append({'when':{d:str(bool(mask&(1<<i))).lower() for i,d in enumerate(directions)},'apply':{'model':ID+':block/tube_frame_'+str(mask)}})
for name in ('swim_tube','passive_pipe','hostile_pipe'):
 p=ROOT/'assets'/ID/'blockstates'/f'{name}.json';m=json.loads(p.read_text());m['multipart']+=pipeframes
 for part in m['multipart']:
  if isinstance(part['apply'],list):part['apply'].sort(key=lambda a:0 if 'water_' in a['model'] else 1)
 p.write_text(json.dumps(m,indent=2)+'\n')
for name in ('aquarium','passive_terrarium','hostile_terrarium','swim_tube','passive_pipe','hostile_pipe'):
 pipe=name.endswith(('tube','pipe'));a=[0,2,2] if pipe else [0,0,0];b=[16,14,14] if pipe else [16,16,16]
 hostile=name.startswith('hostile');passive=name.startswith('passive')
 textures={**frame_tex,**water_tex,'glass':{'sprite':ID+':block/'+('hostile_' if hostile else '')+'glass_clear','force_translucent':True},'dirt':'minecraft:block/dirt','grass':'minecraft:block/grass_block_top','rock':'minecraft:block/stone'}
 shell=[box(a,b,'glass')]+[edge_box(a,b,d,e,.65) for d,e in combinations(directions,2) if axes[d]!=axes[e]];elements=[]
 if passive:
  elements.append(box([a[0]+.7,a[1]+.7,a[2]+.7],[b[0]-.7,a[1]+2.7,b[2]-.7],'dirt'))
  grass=box([a[0]+.7,a[1]+2.7,a[2]+.7],[b[0]-.7,a[1]+3.2,b[2]-.7],'grass')
  for face in grass['faces'].values():face['tintindex']=0
  elements.append(grass)
 elif hostile:
  for x,z,w,h in [(2,4,5,4),(8,8,5,3),(8,4,3,2)]:elements.append(box([x,a[1]+.65,z],[x+w,a[1]+.65+h,z+w],'rock'))
 else:elements.append(box([v+.7 for v in a],[v-.7 for v in b],'water'))
 write(Path('assets')/ID/'models/item'/f'{name}.json',{'parent':'minecraft:block/block','ambientocclusion':False,'textures':textures,'elements':elements+shell,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.7,.7,.7]}}})
 item={'type':'minecraft:model','model':ID+':item/'+name}
 if passive:item['tints']=[{'type':'minecraft:constant','value':0x79B544}]
 write(Path('assets')/ID/'items'/f'{name}.json',{'model':item})
meta=json.loads(metapath.read_text());meta['version']='0.4.0';metapath.write_text(json.dumps(meta,indent=2)+'\n')
