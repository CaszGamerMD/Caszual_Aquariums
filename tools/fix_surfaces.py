"""0.4.1: joined frame surfaces, two-sided pipe glass, depth-safe clear pixels."""
from itertools import product
for prefix,color in [('',(177,219,228)),('hostile_', (167,137,205))]:
 pixels=[[(*color,255) if 2<=x<=13 and 2<=y<=13 and x-y in (2,3) else (0,0,0,0) for x in range(16)] for y in range(16)]
 for mask in range(16):png(prefix+'glass_connected_'+str(mask),pixels)
 png(prefix+'glass_clear',pixels)

def union_surface(boxes,open_dirs=()):
 """Boundary of an axis-aligned bar union, without duplicate/internal faces."""
 if not boxes:return []
 coords=[sorted({e[k][axis] for e in boxes for k in ('from','to')}) for axis in range(3)];occupied=set()
 for cell in product(*(range(len(c)-1) for c in coords)):
  mid=[(coords[a][cell[a]]+coords[a][cell[a]+1])/2 for a in range(3)]
  if any(all(e['from'][a]<mid[a]<e['to'][a] for a in range(3)) for e in boxes):occupied.add(cell)
 result=[]
 for cell in sorted(occupied):
  lo=[coords[a][cell[a]] for a in range(3)];hi=[coords[a][cell[a]+1] for a in range(3)];faces={}
  for d,step in zip(directions,steps):
   if tuple(cell[a]+step[a] for a in range(3)) in occupied:continue
   axis=axes[d];boundary=hi[axis] if d in positive else lo[axis]
   if d in open_dirs and boundary in (0,16):continue
   faces[d]={'texture':'#frame'}
  if faces:result.append({'from':lo,'to':hi,'faces':faces})
 return result

tankparts=[]
for mask in range(64):
 opened={d for i,d in enumerate(directions) if mask&(1<<i)}
 bars=[edge_box([0,0,0],[16,16,16],d,e) for d,e in combinations(directions,2) if axes[d]!=axes[e] and d not in opened and e not in opened]
 model(f'frame_shell_{mask}',union_surface(bars,opened),frame_tex)
 tankparts.append({'when':{d:str(d in opened).lower() for d in directions},'apply':{'model':ID+':block/frame_shell_'+str(mask)}})
for name in ('aquarium','passive_terrarium','hostile_terrarium'):
 p=ROOT/'assets'/ID/'blockstates'/f'{name}.json';m=json.loads(p.read_text());m['multipart']=[part for part in m['multipart'] if ':block/frame_' not in part['apply']['model']]+tankparts;p.write_text(json.dumps(m,indent=2)+'\n')
for mask in range(64):
 p=ROOT/'assets'/ID/'models/block'/f'tube_frame_{mask}.json';m=json.loads(p.read_text());model(f'tube_frame_{mask}',union_surface(m['elements'],{d for i,d in enumerate(directions) if mask&(1<<i)}),frame_tex)
for prefix in ('','hostile_'):
 for mask in range(64):
  p=ROOT/'assets'/ID/'models/block'/f'{prefix}tube_{mask}.json';m=json.loads(p.read_text());panels=[]
  for e in m['elements']:
   for d in e['faces']:
    lo=e['from'].copy();hi=e['to'].copy();a=axes[d]
    if d in positive:lo[a]=hi[a]-.04
    else:hi[a]=lo[a]+.04
    panels.append({'from':lo,'to':hi,'faces':{d:{'texture':'#glass'},opposite[d]:{'texture':'#glass'}}})
  m['elements']=panels;p.write_text(json.dumps(m,indent=2)+'\n')
for p in (ROOT/'assets'/ID/'models').rglob('*.json'):
 m=json.loads(p.read_text());changed=False
 for k,v in m.get('textures',{}).items():
  if isinstance(v,dict) and 'glass_' in v.get('sprite',''):m['textures'][k]=v['sprite'];changed=True
 if p.parent.name=='block' and p.stem.startswith(('glass_','hostile_glass_')) and 'elements' in m:
  for e in m['elements']:
   e['faces']={d:f for d,f in e['faces'].items() if f['texture'] in ('#front','#back')}
   for a in range(3):
    if e['to'][a]-e['from'][a]==.5:
     if e['from'][a]==0:e['from'][a]=.08
     if e['to'][a]==16:e['to'][a]=15.92
  changed=True
 if p.parent.name=='block' and 'tube_ring_' in p.stem:
  d=p.stem.split('tube_ring_')[-1]
  for e in m['elements']:e['faces']={k:v for k,v in e['faces'].items() if k in (d,opposite[d])}
  changed=True
 if p.parent.name=='item' and p.stem in ('aquarium','swim_tube','passive_terrarium','passive_pipe','hostile_terrarium','hostile_pipe'):
  bars=[e for e in m['elements'] if all(f['texture']=='#frame' for f in e['faces'].values())]
  m['elements']=[e for e in m['elements'] if e not in bars]+union_surface(bars)
  for e in m['elements']:
   if all(f['texture']=='#glass' for f in e['faces'].values()):e['from']=[v+.25 for v in e['from']];e['to']=[v-.25 for v in e['to']]
  changed=True
 if changed:
  if 'elements' in m:m['elements']=[e for e in m['elements'] if e['faces']]
  p.write_text(json.dumps(m,indent=2)+'\n')
# Seal water around each narrow pipe entrance, on the INSIDE of the adjoining tank.
p=ROOT/'assets'/ID/'blockstates/swim_tube.json';state=json.loads(p.read_text())
for d in directions:
 axis=axes[d];uv=[a for a in range(3) if a!=axis];plane=16+TANK_WATER_INSET if d in positive else -TANK_WATER_INSET;elements=[]
 outer0=TANK_WATER_INSET;outer1=16-TANK_WATER_INSET;inner0=TUBE_WATER_MIN;inner1=TUBE_WATER_MAX
 for u0,v0,u1,v1 in [(outer0,outer0,inner0,outer1),(inner1,outer0,outer1,outer1),(inner0,outer0,inner1,inner0),(inner0,inner1,inner1,outer1)]:
  lo=[0]*3;hi=[0]*3;lo[axis]=hi[axis]=plane;lo[uv[0]]=u0;lo[uv[1]]=v0;hi[uv[0]]=u1;hi[uv[1]]=v1
  elements.append({'from':lo,'to':hi,'faces':{opposite[d]:{'texture':'#water'}}})
 model('tank_water_ring_'+d,elements,water_tex)
 # Bridge the half-block between the tube mouth and the tank's water skin.
 lo=[inner0,inner0,inner0];hi=[inner1,inner1,inner1]
 if d in positive:lo[axis]=16;hi[axis]=16+TANK_WATER_INSET
 else:lo[axis]=-TANK_WATER_INSET;hi[axis]=0
 neck=box(lo,hi,'water');neck['faces'].pop(d,None);neck['faces'].pop(opposite[d],None)
 model('tank_water_neck_'+d,[neck],water_tex)
 state['multipart'].insert(0,{'when':{'tank_'+d:'true'},'apply':{'model':ID+':block/tank_water_ring_'+d}})
 state['multipart'].insert(0,{'when':{'tank_'+d:'true'},'apply':{'model':ID+':block/tank_water_neck_'+d}})
p.write_text(json.dumps(state,indent=2)+'\n')
meta=json.loads(metapath.read_text());meta['version']='0.4.1';metapath.write_text(json.dumps(meta,indent=2)+'\n')
