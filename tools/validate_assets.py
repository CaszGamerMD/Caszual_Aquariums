"""0.4.2 regression checks: overlapping surfaces, water bounds and model references."""
import json,struct,zlib
from pathlib import Path
from itertools import product
R=Path(__file__).resolve().parents[1]/'src/main/resources/assets/linked_aquariums'
axes={'down':1,'up':1,'north':2,'south':2,'west':0,'east':0};pos={'up','south','east'};dirs=list(axes);steps=[(0,-1,0),(0,1,0),(0,0,-1),(0,0,1),(-1,0,0),(1,0,0)]
def read(n):return json.loads((R/'models/block'/f'{n}.json').read_text())
def no_overlap(elements):
 groups={}
 for e in elements:
  for d in e['faces']:
   a=axes[d];plane=e['to'][a] if d in pos else e['from'][a];uv=[i for i in range(3) if i!=a];rect=tuple((e['from'][i],e['to'][i]) for i in uv);group=groups.setdefault((d,plane),[])
   assert not any(all(min(rect[j][1],other[j][1])-max(rect[j][0],other[j][0])>1e-7 for j in range(2)) for other in group),(d,plane,rect)
   group.append(rect)
coords=[0,2,14,16]
for mask in range(64):
 for name in (f'frame_shell_{mask}',f'tube_frame_{mask}'):no_overlap(read(name)['elements'])
 for prefix in ('','hostile_'):
  m=read(f'{prefix}tube_{mask}');assert m['elements']
  for e in m['elements']:
   assert len(e['faces'])==2;ds=list(e['faces']);assert axes[ds[0]]==axes[ds[1]] and (ds[0] in pos)!=(ds[1] in pos)
   assert abs(e['to'][axes[ds[0]]]-e['from'][axes[ds[0]]]-.04)<1e-6
 cells={(1,1,1)}|{tuple(1+x for x in v) for i,v in enumerate(steps) if mask&(1<<i)};bounds=[([coords[c[a]] for a in range(3)],[coords[c[a]+1] for a in range(3)]) for c in cells];water=read(f'tube_water_{mask}')['elements']
 for w in water:
  for point in product(*[(w['from'][a],w['to'][a]) for a in range(3)]):assert any(all(lo[a]<=point[a]<=hi[a] for a in range(3)) for lo,hi in bounds)
 for i,d in enumerate(dirs):
  if mask&(1<<i):assert any(e['to' if d in pos else 'from'][axes[d]]==(16 if d in pos else 0) for e in water)
for p in (R/'models').rglob('*.json'):
 m=json.loads(p.read_text())
 for e in m.get('elements',[]):assert 1<=len(e['faces'])<=6,p
 for k,v in m.get('textures',{}).items():
  if k!='particle' and isinstance(v,dict) and 'glass' in v.get('sprite',''):assert v.get('force_translucent') is True
 if p.parent.name=='item':no_overlap([e for e in m.get('elements',[]) if all(f['texture']=='#frame' for f in e['faces'].values())])
for p in (R/'blockstates').glob('*.json'):
 m=json.loads(p.read_text());applies=list(m.get('variants',{}).values())+[part['apply'] for part in m.get('multipart',[])]
 for apply in applies:
  for a in apply if isinstance(apply,list) else [apply]:assert (R/'models'/ (a['model'].split(':')[1]+'.json')).exists(),a
for p in (R/'textures/block').glob('*glass*.png'):
 data=p.read_bytes();i=8;raw=b''
 while i<len(data):
  n=struct.unpack('>I',data[i:i+4])[0]
  if data[i+4:i+8]==b'IDAT':raw+=data[i+8:i+8+n]
  i+=12+n
 raw=zlib.decompress(raw);expected=(88,48,122,13) if p.stem.startswith('hostile_') else (219,219,219,13)
 assert {tuple(raw[y*65+1+x*4:y*65+5+x*4]) for y in range(16) for x in range(16)}=={expected},p
parts=json.loads((R/'blockstates/swim_tube.json').read_text())['multipart']
assert all(isinstance(p['apply'],dict) for p in parts),'Pipe models must be simultaneous multipart layers, never random choices'
for mask in range(64):
 flags={d:str(bool(mask&(1<<i))).lower() for i,d in enumerate(dirs)}
 matches=[p['apply']['model'] for p in parts if p['when']==flags]
 assert matches.count(f'linked_aquariums:block/tube_{mask}')==1
 assert matches.count(f'linked_aquariums:block/tube_water_{mask}')==1
 assert matches.count(f'linked_aquariums:block/tube_frame_{mask}')==1
for i in range(1,20):
 body,top=read(f'soil_{i}')['elements'];assert body['from']==[.6,.6,.6] and body['to']==[15.4,2,15.4]
 assert 'up' not in body['faces'] and top['from']==[0,2,0] and top['to']==[16,2,16]
 for mask in range(64):no_overlap(read(f'frame_shell_{mask}')['elements']+[body,top])
for d in dirs:
 m=read('tank_water_ring_'+d);no_overlap(m['elements']);axis=axes[d]
 for e in m['elements']:
  assert e['from'][axis]==e['to'][axis]==(16.55 if d in pos else -.55)
  for a in range(3):
   if a!=axis:assert .55<=e['from'][a]<e['to'][a]<=15.45
for i in range(1,20):assert all('cullface' not in f for e in read(f'soil_{i}')['elements'] for f in e['faces'].values())
for name in ('aquarium','passive_terrarium','hostile_terrarium'):
 parts=json.loads((R/'blockstates'/f'{name}.json').read_text())['multipart'];assert len([p for p in parts if ':block/frame_shell_' in p['apply']['model']])==64
for prefix in ('passive','hostile'):
 for suffix in ('terrarium','pipe'):
  for p in json.loads((R/'blockstates'/f'{prefix}_{suffix}.json').read_text())['multipart']:
   for a in p['apply'] if isinstance(p['apply'],list) else [p['apply']]:assert not a['model'].split(':block/')[-1].startswith(('water_','tube_water_','tank_water_ring_'))
print('PASS: 64 frame unions without overlapping faces; 128 two-sided pipe shells; bounded full tube water; six tank-side water seals; uniform requested colors at 5% glass opacity; simultaneous glass/water layers; inset seamless floors and dry terrariums.')
